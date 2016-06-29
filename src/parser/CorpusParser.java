/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import model.Phrase;

/**
 *
 * @author macair
 */
public class CorpusParser {
    
    private Phrase phrase;
    private BufferedReader br;
    private BufferedWriter bw ;
    
    public static void main(String[] args) throws Exception{
        CorpusParser parser = new CorpusParser();
        parser.neTaggedtoXML("corpus tagged.txt", "corpus.xml");
    }
    
    public void neTaggedtoXML(String inputFile, String outputFile) throws Exception{
        br = new BufferedReader(new FileReader(inputFile));
        bw = new BufferedWriter(new FileWriter(outputFile));
        
        bw.write("<?xml version='1.0' encoding='UTF-8'?>\n\n");
        bw.write("<data>\n");
        
        String line;
	int lineCounter = 0;
        while((line = br.readLine()) != null){
//            System.out.println();
	    lineCounter++;
            process(line, 0, lineCounter);
        }
        
        bw.write("</data>");
        bw.close();
        br.close();
    }
    
    public void process(String str, int startIdx, int count) throws Exception{
        int idx1 = str.indexOf("(", startIdx) + 1;
        int idx2 = str.indexOf(" ", idx1);
        String tag = str.substring(idx1, idx2);
        idx1 = idx2+1;
        idx2 = getEndidx(str, idx1);
        phrase = new Phrase(str.substring(idx1, idx2), tag, true);
        String output = phrase.toString().replace(" <", "<");
//        System.out.println(output);
        bw.write("<sentence id=\"" + count + "\">\n" + output + "\n</sentence>\n");
    }
    
    public int getEndidx(String str, int curIdx){
        int counter = 1;
        int idx = curIdx;
        while(counter > 0){
            if(str.charAt(idx) == '(')
                counter++;
            else if(str.charAt(idx) == ')')
                counter--;
            idx++;
        }
        return idx-1;
    }
}
