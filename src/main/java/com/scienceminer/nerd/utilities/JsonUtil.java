package com.scienceminer.nerd.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.scienceminer.nerd.disambiguation.NerdEntity;
import com.scienceminer.nerd.evaluation.NEDCorpusEvaluation;
import com.scienceminer.nerd.kb.UpperKnowledgeBase;
import org.apache.commons.io.FileUtils;
import org.grobid.core.lexicon.NERLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


// a class to extract entity-fishing results in JSON format
public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    public List<NerdEntity> parseFromJsonFile(File inputFile) {
        List<NerdEntity> entityList = new ArrayList<NerdEntity>();

        try {
            Scanner myReader = new Scanner(inputFile);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(data);
                ArrayNode entitiesNode = (ArrayNode) node.get("entities");
                System.out.println(entitiesNode);
                for (int i = 0; i < entitiesNode.size(); i++) {
                    NerdEntity nerdEntity = new NerdEntity();

                    // get rawName
                    JsonNode rawName = entitiesNode.get(i).get("rawName");
                    if (rawName != null) {
                        nerdEntity.setRawName(rawName.toString().replace("\"", ""));
                    } else {
                        nerdEntity.setRawName("");
                    }

                    // get typeNEEntityFishing --> nerGrobidType
                    JsonNode type = entitiesNode.get(i).get("type");
                    if (type != null) {
                        //nerdEntity.setTypeFromString(type.toString().toUpperCase());
                        nerdEntity.setType(NERLexicon.NER_Type.valueOf(type.toString().replace("\"", "").toUpperCase()));
                    } else {
                        nerdEntity.setTypeFromString("UNKNOWN");
                    }

                    // get offsetStart
                    JsonNode offsetStart = entitiesNode.get(i).get("offsetStart");
                    if (offsetStart != null) {
                        nerdEntity.setOffsetStart(Integer.parseInt(offsetStart.toString().replace("\"", "")));
                    } else {
                        nerdEntity.setOffsetStart(-1);
                    }

                    // get offsetEnd
                    JsonNode offsetEnd = entitiesNode.get(i).get("offsetEnd");
                    if (offsetEnd != null) {
                        nerdEntity.setOffsetEnd(Integer.parseInt(offsetEnd.toString().replace("\"", "")));
                    } else {
                        nerdEntity.setOffsetEnd(-1);
                    }

                    // get nerd_score
                    JsonNode nerd_score = entitiesNode.get(i).get("nerd_score");
                    if (nerd_score != null) {
                        nerdEntity.setNerdScore(Double.parseDouble(nerd_score.toString().replace("\"", "")));
                    } else {
                        nerdEntity.setNerdScore(0.0);
                    }

                    // get nerd_selection_score
                    JsonNode nerd_selection_score = entitiesNode.get(i).get("nerd_score");
                    if (nerd_selection_score != null) {
                        nerdEntity.setSelectionScore(Double.parseDouble(nerd_selection_score.toString().replace("\"", "")));
                    } else {
                        nerdEntity.setSelectionScore(0.0);
                    }

                    // get wikipediaExternalRef
                    JsonNode wikipediaExternalRef = entitiesNode.get(i).get("wikipediaExternalRef");
                    if (wikipediaExternalRef != null) {
                        nerdEntity.setWikipediaExternalRef(Integer.parseInt(wikipediaExternalRef.toString().replace("\"", "")));
                    } else {
                        nerdEntity.setWikipediaExternalRef(-1);
                    }

                    // get wikidataId
                    JsonNode wikidataId = entitiesNode.get(i).get("wikidataId");
                    if (wikidataId != null) {
                        nerdEntity.setWikidataId(wikidataId.toString().replace("\"", ""));
                    } else {
                        nerdEntity.setWikidataId(null);
                    }

                    //put the result into List
                    entityList.add(nerdEntity);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create a list with the distinct elements
        List<NerdEntity> listDistinct = entityList.stream().distinct().collect(Collectors.toList());
        return listDistinct;
    }

    public void entitiesToFile(List<NerdEntity> entityList, String outputFile) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write("RawName \t" + "NERType \t" + "WikidataID \n");
            for (int i = 0; i < entityList.size(); i++) {
                writer.write(entityList.get(i).getRawName() + "\t" +
                        entityList.get(i).getType().getName() + "\t" +
                        (entityList.get(i).getWikidataId()) + "\n");
            }

        } catch (Exception e) {
            LOGGER.info("Some errors encountered when saving to a Csv file in \"" + outputFile + "\"", e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String pathInputFolder = args[0];
        File dir = new File(pathInputFolder);
        String outputFile = "";

        if (pathInputFolder.length() == 0 || pathInputFolder == null) {
            System.err.println("Usage: command [pathInputDirectory] [pathOutputFile]");
            System.exit(-1);
        } else {
            outputFile = dir.getCanonicalPath() + "/extractedEntityList.txt";
            if (args.length == 2) {
                if (args[1] != null && args[1].length() != 0) {
                    outputFile = args[1];
                }
            }
            JsonUtil jsonUtil = new JsonUtil();
            List<NerdEntity> entityList = new ArrayList<NerdEntity>();

            String[] extensions = new String[]{"json"};
            System.out.println("Getting all .json files in " + dir.getCanonicalPath()
                    + " including those in subdirectories");
            List<File> listOfFiles = (List<File>) FileUtils.listFiles(dir, extensions, true);
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println("Extracting file: " + file.getCanonicalPath());
                    entityList.addAll(jsonUtil.parseFromJsonFile(file));

                    // Create a list with the distinct elements
                    List<NerdEntity> listDistinct = entityList.stream().distinct().collect(Collectors.toList());
                    jsonUtil.entitiesToFile(listDistinct, outputFile);
                } else {
                    System.err.println("Directory " + dir + "doesn't exist");
                    System.exit(-1);
                }
            }
        }
    }
}
