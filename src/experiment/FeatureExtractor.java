/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 *
 * @author macair
 */
public class FeatureExtractor {
    
    static BufferedWriter bw;
    static List<Integer> dotIdx;
    static List<Integer> commaIdx;
    static List<Integer> quoteIdx;
    
    public static void main(String[] args) throws Exception{
	bw = new BufferedWriter(new FileWriter("feature.arff"));
	File file = new File("corpus_ne_simple_reweight.txt.xml");
        SAXReader reader = new SAXReader();
	Document document = reader.read(file);
	
	List<Node> sentences = document.selectNodes("/data/sentence");
	
	for(int i=0; i<sentences.size(); i++){
	    process(i, sentences);
	    System.out.println("sentence " + i);
	}
	bw.close();
    }
    
    public static void process(int curIdx, List<Node> sentences) throws Exception{
	ArrayList<Node> phrases = new ArrayList<>();
	phrases.addAll(sentences.get(curIdx).selectNodes("phrase"));
        
	for(int i=curIdx+1; i<curIdx+5; i++){
	    if(i >= sentences.size())
		break;
	    List<Node> nodes = sentences.get(i).selectNodes("phrase");
	    if(nodes != null && nodes.size() > 0)
		phrases.addAll(nodes);
	}
        
        quoteIdx = new ArrayList<>();
        dotIdx = new ArrayList<>();
        commaIdx = new ArrayList<>();
        for(int i=0; i<phrases.size(); i++){
            String[] words = phrases.get(i).getText().split(" ");
            String phraseString = words[words.length-1].split("\\\\")[0];
            if(phraseString.endsWith(".")){
                dotIdx.add(i);
            }else if(phraseString.endsWith(",")){
                commaIdx.add(i);
            }else if(phraseString.equals("\"")){
                quoteIdx.add(i);
            }
//            System.out.println(phrases.get(i).getText());
//            System.out.println(dotIdx.size());
        }
        
	extractFeature(phrases);
    }
    
    public static void extractFeature(List<Node> phrases) throws Exception{
	for(int i=0; i<phrases.size() - 1; i++){
	    Node p = phrases.get(i);
	    Node p2 = null;
	    if(!p.valueOf("@type").contains("np"))
		continue;
	    for(int j=i+1; j<phrases.size(); j++){
		p2 = phrases.get(j);
		if(!p2.valueOf("@type").contains("np")){
		    p2 = null;
		    continue;
		}
		if(p2 != null){
		    extractLexicalFeature(p, p2, i, j);
		    bw.write(p.getText() + ", " + p2.getText() + "\n");
		}
	    }
	}
    }
    
    public static int getDistance(String phrase1, String phrase2, int phraseIdx1, int phraseIdx2){
        int counter = 0;
//        System.out.println(phrase1 + " " + phrase2);
        for(int i=0; i<dotIdx.size(); i++){
//            System.out.println(phraseIdx1 + " " + dotIdx.get(i) + " " + phraseIdx2 + " " + counter);
            if(phraseIdx1 > dotIdx.get(i)){
                continue;
            }else if(phraseIdx2 <= dotIdx.get(i)){
                break;
            }else{
                counter++;
            }
        }
        return counter;
    }
    
    // substring match, ne match, s1 pronoun, s2 pronoun, s1 proper name, s2 proper name
    // distance, 
    public static void extractLexicalFeature(Node n1, Node n2, int idx1, int idx2) throws Exception{
	bw.write(extractFeature1(n1.getText(), n2.getText()) + ", ");
        bw.write(extractFeature2(n1, n2) + ", ");
	bw.write(extractFeature3(n1.getText(), n2.getText()) + ", ");
	bw.write(extractFeature4(n1.getText(), n2.getText()) + ", ");
        bw.write(getDistance(n1.getText(), n2.getText(), idx1, idx2) + ", ");
        bw.write(extractFeature6(n1.getText(), idx1, idx2) + ", ");
//        bw.write(extractFeature7(n1, n2) + ", ");
        bw.write(extractFeature8(n1.getText()) + ", ");
        bw.write(extractFeature8(n2.getText()) + ", ");
        bw.write(extractFeature9(idx1) + ", ");
        bw.write(extractFeature9(idx2) + ", ");
        bw.write(extractFeature10(idx1, idx2) + ", ");
    }
    
    // fitur substring match
    public static boolean extractFeature1(String s1, String s2){
	String str1 = s1.replaceAll("\\\\[\\w]+", "");
	String str2 = s2.replaceAll("\\\\[\\w]+", "");
	String[] a1 = str1.split(" ");
	String[] a2 = str2.split(" ");
	boolean found = false;
	for(String a : a1){
	    for(String b : a2){
		if(a.contains(b) || b.contains(a)){
		    found = true;
		    break;
		}
	    }
	    if(found)
		break;
	}
	return found;
    }
    
    // fitur same entity type
    public static boolean extractFeature2(Node n1, Node n2){
	String[] neList1 = n1.valueOf("@ne").split("\\|");
        String[] neList2 = n2.valueOf("@ne").split("\\|");
        boolean match = false;
        for(String ne1 : neList1){
            for(String ne2 : neList2){
                if(ne1.equals(ne2)){
                    match = true;
                    break;
                }
            }
            if(match){
                break;
            }
        }
        return match;
    }
    
    // fitur isPronoun
    public static String extractFeature3(String s1, String s2){
	String retval;
	if(s1.contains("\\PR")){
	    retval = "true";
	}else{
	    retval = "false";
	}
	if(s2.contains("\\PR")){
	    retval += ", true";
	}else{
	    retval += ", false";
	}
	return retval;
    }
    
    // fitur is proper name
    public static String extractFeature4(String s1, String s2){
	String retval;
	if(s1.contains("\\NNP")){
	    retval = "true";
	}else{
	    retval = "false";
	}
	if(s2.contains("\\NNP")){
	    retval += ", true";
	}else{
	    retval += ", false";
	}
	return retval;
    }
    
    public static boolean extractFeature6(String s1, int idx1, int idx2){
        // frase bersebelahan dan frase 1 diakhiri tanda koma
        if(idx2 - idx1 == 1 && s1.split("\\\\")[0].endsWith(",")){
            return true;
        }
        return false;
    }
    
//    public static boolean extractFeature7(Node n1, Node n2){
//        
//    }
    
    public static boolean extractFeature8(String str){
        String s = str.split("\\\\")[0].toLowerCase();
        return (s.equals("aku") || s.equals("saya") || s.equals("beta"));
    }
    
    public static boolean extractFeature9(int strIdx){
        int openIdx = -1, closeIdx = -1;
        for(int i=0; i<quoteIdx.size(); i++){
            if(openIdx == -1){
                openIdx = quoteIdx.get(i);
            }else if(closeIdx == -1){
                closeIdx = quoteIdx.get(i);
            }else{
                if(openIdx <= strIdx && closeIdx >= strIdx){
                    return true;
                }else{
                    openIdx = -1;
                    closeIdx = -1;
                }
            }
        }
        return false;
    }
    
    public static boolean extractFeature10(int idx1, int idx2){
        return idx2 - idx1 == 1;
    }
}
