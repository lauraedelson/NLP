import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;


public class GreedyPOS {

    public static void main(String[] args) {

        String START = "start";
        String END = "end";

        if (args.length < 3) {
            System.out.print("Usage: Prog1 training_file.txt testing_file.txt output_file.txt");
            System.exit(1);
        }

        String training_path = args[0];
        String testing_path = args[1];
        String output_path = args[2];
        HashMap<String, HashMap<String, Integer>> pos_word_map = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, Integer> pos_count_map = new HashMap<String, Integer> ();
        HashMap<String, HashMap<String, Integer>> pos_bigrams = new HashMap<String, HashMap<String, Integer>>();
        HashMap<ArrayList<String>, HashMap<String, Integer>> pos_trigrams = new HashMap<ArrayList<String>, HashMap<String, Integer>>();
        Integer total_pos_count = 0;
        //read training file
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(training_path);
        } catch (FileNotFoundException e) {
            System.out.print("Training file not found");
            System.exit(1);
        }
        sc = new Scanner(inputStream, "UTF-8");
        String prev_pos = START;
        String prev_prev_pos = "";
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\t");
            if (parts.length < 2) {
                if (pos_bigrams.containsKey(prev_pos)) {
                    if (pos_bigrams.get(prev_pos).containsKey(END)) {
                        Integer count = pos_bigrams.get(prev_pos).get(END);
                        pos_bigrams.get(prev_pos).put(END, count +1);
                    } else {
                        pos_bigrams.get(prev_pos).put(END, 1);
                    }
                }else {
                    HashMap<String, Integer> to_map = new HashMap<String, Integer>();
                    to_map.put(END, 1);
                    pos_bigrams.put(prev_pos, to_map);
                }

                if (prev_prev_pos != "") {
                    ArrayList<String> trigram_key = new ArrayList<String>();
                    trigram_key.add(prev_prev_pos);
                    trigram_key.add(prev_pos);
                    if (pos_trigrams.containsKey(trigram_key)) {
                        if (pos_trigrams.get(trigram_key).containsKey(END)) {
                            Integer count = pos_trigrams.get(trigram_key).get(END);
                            pos_trigrams.get(trigram_key).put(END, count +1);
                        } else {
                            pos_trigrams.get(trigram_key).put(END, 1);
                        }
                    }else {
                        HashMap<String, Integer> to_map = new HashMap<String, Integer>();
                        to_map.put(END, 1);
                        pos_trigrams.put(trigram_key, to_map);
                    }
                }

                prev_prev_pos = "";
                prev_pos = START;
                continue;
            }
            total_pos_count +=1;
            String word = parts[0];
            String pos = parts[1];

            if (pos_count_map.containsKey(pos)) {
                Integer pos_count = pos_count_map.get(pos);
                pos_count_map.put(pos, pos_count + 1);
                if (pos_word_map.get(pos).containsKey(word)) {
                    Integer word_count = pos_word_map.get(pos).get(word);
                    pos_word_map.get(pos).put(word, word_count + 1);
                } else {
                    pos_word_map.get(pos).put(word, 1);
                }
            } else {
                pos_count_map.put(pos, 1);
                HashMap<String, Integer> curr_word_map = new HashMap<String, Integer>();
                curr_word_map.put(word, 1);
                pos_word_map.put(pos, curr_word_map);
            }
            if (pos_bigrams.containsKey(prev_pos)) {

                if (pos_bigrams.get(prev_pos).containsKey(pos)) {
                    Integer count = pos_bigrams.get(prev_pos).get(pos);
                    pos_bigrams.get(prev_pos).put(pos, count + 1);
                }else {
                    pos_bigrams.get(prev_pos).put(pos, 1);
                }
            }else {
                HashMap<String, Integer> to_map = new HashMap<String, Integer>();
                to_map.put(pos, 1);
                pos_bigrams.put(prev_pos, to_map);
            }

            if (prev_prev_pos != "") {
                ArrayList<String> trigram_key = new ArrayList<String>();
                trigram_key.add(prev_prev_pos);
                trigram_key.add(prev_pos);
                if (pos_trigrams.containsKey(trigram_key)) {
                    if (pos_trigrams.get(trigram_key).containsKey(pos)) {
                        Integer count = pos_trigrams.get(trigram_key).get(pos);
                        pos_trigrams.get(trigram_key).put(pos, count +1);
                    } else {
                        pos_trigrams.get(trigram_key).put(pos, 1);
                    }
                }else {
                    HashMap<String, Integer> to_map = new HashMap<String, Integer>();
                    to_map.put(pos, 1);
                    pos_trigrams.put(trigram_key, to_map);
                }
            }
            prev_prev_pos = prev_pos;
            prev_pos = pos;
        }
        // note that Scanner suppresses exceptions
        if (sc.ioException() != null) {
            System.out.print("Error while scanning");
            System.exit(1);
        }

        HashMap<String, HashMap<String, Double>> word_pos_probabilities = new HashMap<String, HashMap<String, Double>>();
        HashMap<String, Double> pos_unigram_probabilities = new HashMap<>();
        HashMap<String, HashMap<String, Double>> pos_bigram_probabilities = new HashMap<String, HashMap<String, Double>>();
        HashMap<ArrayList<String>, HashMap<String, Double>> pos_trigram_probabilities = new HashMap<ArrayList<String>, HashMap<String, Double>>();

        for (Entry<String, HashMap<String, Integer>> entry : pos_word_map.entrySet())
        {
            String pos = entry.getKey();
            Integer pos_count = pos_count_map.get(pos);
            for (Entry<String, Integer> word_entry : entry.getValue().entrySet()) {
                String word = word_entry.getKey();
                Integer count = word_entry.getValue();
                if (word_pos_probabilities.containsKey(word)) {
                    word_pos_probabilities.get(word).put(pos, count.doubleValue()/pos_count.doubleValue());
                }else {
                    HashMap<String, Double> pos_map = new HashMap<String, Double>();
                    pos_map.put(pos, count.doubleValue()/pos_count.doubleValue());
                    word_pos_probabilities.put(word, pos_map);
                }
            }
        }

        for (Entry<String, Integer> entry : pos_count_map.entrySet()) {
            pos_unigram_probabilities.put(entry.getKey(), entry.getValue().doubleValue()/total_pos_count.doubleValue());
        }

        for (Entry<String, HashMap<String, Integer>> entry : pos_bigrams.entrySet()) {
            String from = entry.getKey();
            Integer transition_total = entry.getValue().values().stream().mapToInt(i -> i).sum();
            for (Entry<String, Integer> pos_entry : entry.getValue().entrySet()) {
                String to = pos_entry.getKey();
                Integer to_count = pos_entry.getValue();
                if (pos_bigram_probabilities.containsKey(from)) {
                    pos_bigram_probabilities.get(from).put(to,to_count.doubleValue()/transition_total.doubleValue());
                } else {
                    HashMap<String, Double> to_map = new HashMap<String, Double>();
                    to_map.put(to, to_count.doubleValue()/transition_total.doubleValue());
                    pos_bigram_probabilities.put(from, to_map);
                }
            }
        }

        for (Entry<ArrayList<String>, HashMap<String, Integer>> entry : pos_trigrams.entrySet()) {
            ArrayList<String> from = entry.getKey();
            Integer transition_total = entry.getValue().values().stream().mapToInt(i -> i).sum();
            for (Entry<String, Integer> pos_entry : entry.getValue().entrySet()) {
                String to = pos_entry.getKey();
                Integer to_count = pos_entry.getValue();
                if (pos_trigram_probabilities.containsKey(from)) {
                    pos_trigram_probabilities.get(from).put(to,to_count.doubleValue()/transition_total.doubleValue());
                } else {
                    HashMap<String, Double> to_map = new HashMap<String, Double>();
                    to_map.put(to, to_count.doubleValue()/transition_total.doubleValue());
                    pos_trigram_probabilities.put(from, to_map);
                }
            }
        }


        //open output file
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(output_path))) {
            //read test file
            FileInputStream testInputStream = null;
            Scanner testSc = null;
            try {
                testInputStream = new FileInputStream(testing_path);
            } catch (FileNotFoundException e) {
                System.out.print("Training file not found");
                System.exit(1);
            }

            testSc = new Scanner(testInputStream, "UTF-8");
            prev_prev_pos = "";
            prev_pos = START;
            while (testSc.hasNextLine()) {
                String word = testSc.nextLine();
                if (word.length() < 1) {
                    prev_prev_pos = "";
                    prev_pos = START;
                    out.write("\n");
                    continue;
                }

                //get probabilities for words given POS
                HashMap<String, Double> pos_prob_map = word_pos_probabilities.get(word);

                if (prev_pos == END) {
                    prev_prev_pos = "";
                    prev_pos = START;
                }

                //get state transition probabilities
                HashMap<String, Double> trans_prob_map = null;
                /*if (prev_prev_pos != "") {
                    ArrayList<String> trigram_key = new ArrayList<String>();
                    trigram_key.add(prev_prev_pos);
                    trigram_key.add(prev_pos);
                    trans_prob_map = pos_trigram_probabilities.get(trigram_key);
                }*/
                if (trans_prob_map == null) { //we didn't find this in the trigram map, let's try the bigram map
                    trans_prob_map = pos_bigram_probabilities.get(prev_pos);
                }

                //predict test set, write to file output
                HashMap<String, Double> pred_map = new HashMap<String, Double>();
                String max_pos = "";
                Double max_pred = 0.0;
                if (pos_prob_map != null) { //We seen this word before
                    for (Entry<String, Double> pos_entry : pos_prob_map.entrySet()) {
                        String pos = pos_entry.getKey();
                        Double prob = pos_entry.getValue();
                        Double trans_prob = trans_prob_map.get(pos);
                        //if (trans_prob == null) trans_prob = pos_unigram_probabilities.get(prev_pos);
                        if (trans_prob == null) trans_prob = 0.0;

                        Double pred = prob * trans_prob;
                        pred_map.put(pos, pred);
                        if (pred >= max_pred) {
                            max_pred = pred;
                            max_pos = pos;
                        }
                    }
                } else {
                    for (Entry<String, Double> trans_entry : trans_prob_map.entrySet()) {
                        String pos = trans_entry.getKey();
                        Double prob = trans_entry.getValue();
                        if (prob > max_pred) {
                            max_pred = prob;
                            max_pos = pos;
                        }
                    }

                    //if the word has a hypen, it's an adjective
                    if (word.contains("-")) {
                        max_pos = "JJ";
                    }
                    //if the word is capitalized, it's probably a proper noun
                    if (word != word.toLowerCase()) {
                        max_pos = "NNP";
                    }

                }
                out.write(word + "\t" + max_pos + "\n");
                prev_prev_pos = prev_pos;
                prev_pos = max_pos;
            }
            // note that Scanner suppresses exceptions
            if (testSc.ioException() != null) {
                System.out.print("Error while scanning");
                System.exit(1);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}