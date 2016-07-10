/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author macair
 */
public class XMLCorpusParser {
    
    
    public static void main(String[] args) throws Exception{
        XMLCorpusParser parser = new XMLCorpusParser();
        
//        String neFile = "corpus_ne_simple_reweight.txt";
//        parser.insertNETag("corpus_complete.xml", neFile, neFile + ".xml");

        parser.xmlToCSV("corpus_ne_simple_reweight.txt.xml", "coref_simple_reweight.csv");

//        parser.completeXMLTag("corpus.xml", "corpus_complete.xml");

//        System.out.println(parser.completeTag("Selama\\IN<phrase type=\"np\" id=\"43\">bertahun-tahun\\CD</phrase><phrase type=\"np-sbj\" id=\"44\">monyet\\NN</phrase>mengganggu\\VB<phrase type=\"np\" id=\"45\">warga\\NN Delhi\\NNP</phrase>.\\Z "));
    }
    
    public void insertNETag(String xmlFile, String NEFile, String outputFile) throws Exception{
        File file = new File(xmlFile);
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(file);
        BufferedReader reader = new BufferedReader(new FileReader(NEFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        int currLine = 0;
        int currIdx = 0;
        String line;
        
        List<Node> sentences = document.selectNodes("/data/sentence");
        while((line = reader.readLine()) != null){
            String[] neWords = line.split("\\s+");
            List<Node> phrases = sentences.get(currLine).selectNodes("phrase");
            System.out.println("line " + line);
            System.out.println("xml" + sentences.get(currLine).asXML());
            for(Node phrase : phrases){
                List<String> neTags = new ArrayList<>();
                String phraseString = phrase.getText();
                String[] words = phraseString.split("[\\s_]+");
                boolean processNE = !phrase.valueOf("@type").equals("other");
                if(processNE){
                    for(String word : words){
                        String neTag = neWords[currIdx].split("/")[1];
                        if(neTag != null && !neTag.equals("") && !neTags.contains(neTag)){
                            neTags.add(neTag);
                        }
                        currIdx++;
                    }
                    int tagCounter = 0;
                    StringBuilder tagBuilder = new StringBuilder();
                    for(String tag : neTags){
                        tagBuilder.append(tag);
                        tagCounter++;
                        if(tagCounter < neTags.size()){
                            tagBuilder.append("|");
                        }
                    }
                    Element phraseElement = (Element) phrase;
                    phraseElement.addAttribute("ne", tagBuilder.toString());
                }else{
                    for(String word : words){
                        currIdx++;
                    }
                }
            }
            currLine++;
            currIdx = 0;
        }
        writer.write(document.asXML());
        writer.close();
        reader.close();
    }
    
    // convert xml -> csv, labelling made easy
    public void xmlToCSV(String inputPath, String outputPath) throws Exception{
	File file = new File(inputPath);
	SAXReader reader = new SAXReader();
	Document document = reader.read(file);
	BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
	
	List<Node> phrases = document.selectNodes("/data/sentence/phrase");
	for(Node n : phrases){
	    String id = n.valueOf("@id");
            String ne = n.valueOf("@ne");
	    String coref = "null";
	    if(id == null || id.equals(""))
		id = "";
	    bw.write(id + "," + ne + ",\"" + n.getText().replaceAll("\\\\\\w+", "") + "\"," + coref + "\n");
	}
	
	bw.close();
    }
    
    // add <phrase type=other> tag to untagged phrases
    public void completeXMLTag(String inputPath, String outputPath) throws Exception{
	BufferedReader br = new BufferedReader(new FileReader(inputPath));
	BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
        
        String line;
        while((line = br.readLine()) != null){
            if(line.contains("phrase")){
                bw.write(completeTag(line).replace(" <", "<") + "\n");
            }else{
		bw.write(line + "\n");
	    }
        }
	
	bw.close();
	br.close();
    }
    
    /*
     * Menambahkan tag <phrase> untuk kata-kata yang masih berada di luar tag
     * (Proses sebelumnya hanya menambahkan tag <phrase> untuk kata-kata yang merupakan bagian dari NP)
     */
    public String completeTag(String line){
	List<Integer> indexes = new ArrayList<>();
	List<String> strings = new ArrayList<>();
	StringBuilder builder = new StringBuilder();
        int idx1 = 0;
        int idx2 = 0;
        while(idx2 < line.length() - 1){
	    // process untagged phrase at sentence's head
	    if(idx1 == 0){
		idx1 = line.indexOf("<phrase");
		if(idx1 > 0){
		    indexes.add(0);
		    indexes.add(idx1);
		}else{
		    idx1 = 1;
		    continue;
		}
	    }else{
		idx1 = line.indexOf("</phrase>", idx2);
                System.out.println(line.substring(0, idx1));
		if(idx1 == -1 || idx1 + 9 > line.length() - 1){
		    break;
		}else{
		    // index after closing tag of phrase
		    idx1 += 9;
		}
		if(line.charAt(idx1) != '<'){
		    idx2 = line.indexOf("<phrase", idx1);
		    if(idx2 == -1){
			idx2 = line.length();
		    }
		    indexes.add(idx1);
		    indexes.add(idx2);
		}else{
                   idx2 = idx1;
		}
	    }
	}
	if(indexes.size() == 0)
	    return line;
	else{
	    if(indexes.get(0) > 0)
		strings.add(line.substring(0, indexes.get(0)));
	}
	for(int i=0; i<indexes.size(); i+=2){
	    strings.add("<phrase type=\"other\">" + line.substring(indexes.get(i), indexes.get(i+1)) + "</phrase>");
	    if(indexes.size() > i+2){
		strings.add(line.substring(indexes.get(i+1), indexes.get(i+2)));
	    }
	}
	if(indexes.get(indexes.size() - 1) < line.length()){
	    strings.add(line.substring(indexes.get(indexes.size() - 1)));
	}
	for(String s : strings){
	    builder.append(s);
	}
	return builder.toString();
    }
}
