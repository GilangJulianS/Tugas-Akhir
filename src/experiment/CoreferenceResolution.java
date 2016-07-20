/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.w3c.dom.css.Counter;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 *
 * @author Gilang
 */
public class CoreferenceResolution {
    
    static List<Node> phrases;
    static List<Node> sentences;
    static Instances dataset;
    static Classifier classifier;
    static int counter = 0;
    static int sentencesIdx = 0;
    
    public static void main(String[] args) throws Exception{
//        addCoreference("corpus_ne_simple_reweight.txt.xml", "coref_j48.model", "feature_header.arff", 20);
        addCoreference("corpus_ne_simple_reweight.txt2.xml", "coref_j48.model", "feature_header.arff", 20);
    }
    
    public static void addCoreference(String xmlFile, String modelFile, String headerFile, int maxSentence) throws Exception{
        // load xml file
        File file = new File(xmlFile);
        SAXReader reader = new SAXReader();
	Document document = reader.read(file);
        
        dataset = new Instances(new BufferedReader(new FileReader(headerFile)));
        dataset.setClassIndex(dataset.numAttributes() - 1);
        classifier = (Classifier) weka.core.SerializationHelper.read(modelFile);
        
        sentences = new ArrayList<>();
        sentences.addAll(document.selectNodes("/data/sentence"));
        
        for(int x=0; x<sentences.size() - 20; x++){
            int phrasesCount = sentences.get(x).selectNodes("phrase").size();
            
            phrases = new ArrayList<>();
            for(int j=x; j<x+maxSentence; j++){
                phrases.addAll(sentences.get(j).selectNodes("phrase"));
            }
            
            FeatureExtractor.setPunctuationIndex(phrases);

            for(int i=0; i<phrasesCount; i++){
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
                        List<String> features = FeatureExtractor.extractLexicalFeature(p, p2, i, j);
                        classifyInstance(features, p, p2);
                    }
                }
            }
        }
    }
    
    public static void classifyInstance(List<String> features, Node n1, Node n2) throws Exception{
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);
        for(int i=0; i<features.size(); i++){
            if(i == 6){
                instance.setValue(i, Integer.valueOf(features.get(i)));
            }else{
                instance.setValue(i, features.get(i));
            }
        }
        double classLabel = classifier.classifyInstance(instance);
        if(classLabel > 0){
            System.out.println(n1.getText() + ", " + n2.getText() + "|||||" + instance.toString());
        }
    }
    
}
