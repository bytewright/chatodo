package de.bytewright.chatodo.backend.chat.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class IntentRecognitionService {

    private final StanfordCoreNLP stanfordCoreNLP;

    public IntentRecognitionService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse");
        this.stanfordCoreNLP = new StanfordCoreNLP(props);
    }

    public MsgClassification classify(String inputText) {
        /*
        ToDo:
            https://medium.com/mysuperai/what-is-intent-recognition-and-how-can-i-use-it-9ceb35055c4f
            https://blogs.oracle.com/javamagazine/post/java-natural-language-intent-recognition
            https://github.com/ankitshaw/athena-PersonalAssistant
            add something fancy like that
            // this will use https://stanfordnlp.github.io/CoreNLP/ for now
        */
        MsgClassification msgClassification = new MsgClassification(inputText);
        Annotation annotation = stanfordCoreNLP.process(inputText);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            SemanticGraph sg = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            String intent = "It does not seem that the sentence expresses an explicit intent.";
            for (SemanticGraphEdge edge : sg.edgeIterable()) {
                if ("direct object".equals(edge.getRelation().getLongName())) {
                    String tverb = edge.getGovernor().originalText();
                    String dobj = edge.getDependent().originalText();
                    dobj = dobj.substring(0, 1).toUpperCase() + dobj.substring(1).toLowerCase();
                    intent = tverb + dobj;

                }
            }
            msgClassification.setIntentClassifiction(intent);
        }
        return msgClassification;
    }

}