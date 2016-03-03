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
    
    private static Phrase phrase;
    private static BufferedReader br;
    private static BufferedWriter bw ;
    
    public static void main(String[] args) throws Exception{
        br = new BufferedReader(new FileReader("corpus tagged.txt"));
        bw = new BufferedWriter(new FileWriter("corpus.xml"));
        
        bw.write("<?xml version='1.0' encoding='UTF-8'?>\n\n");
        bw.write("<DATA>\n");
        
        String line;
        while((line = br.readLine()) != null){
            line = br.readLine();
//            System.out.println(line);
            process(line, 0);
        }
        
        bw.write("</DATA>");
        bw.close();
        br.close();
    }
    
    public static void process(String str, int startIdx) throws Exception{
        int idx1 = str.indexOf("(", startIdx) + 1;
        int idx2 = str.indexOf(" ", idx1);
        String tag = str.substring(idx1, idx2);
        idx1 = idx2+1;
        idx2 = getEndidx(str, idx1);
        phrase = new Phrase(str.substring(idx1, idx2), tag, true);
        String output = phrase.toString();
//        System.out.println(output);
        bw.write("\t<SENTENCE>\n\t\t" + output + "\n\t</SENTENCE>\n");
    }
    
    public static int getEndidx(String str, int curIdx){
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
