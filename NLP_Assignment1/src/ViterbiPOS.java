import cc.mallet.util.CommandOption;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;


public class ViterbiPOS {
    public static void main(String[] args) {
        String START = "start";
        String END = "end";

        if (args.length < 3) {
            System.out.print("Usage: ViterbiPOS training_file.txt testing_file.txt output_file.txt");
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
        HashSet<String> distinct_pos = new HashSet<>();
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
            distinct_pos.add(pos);

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

            if (!prev_prev_pos.equals("")) {
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

        //Calculate probabilities for words that only occur once - we will use this to predict words that never appear
        HashMap<String, String> single_word_pos = new HashMap<>();
        for (Entry<String, HashMap<String, Integer>> entry : pos_word_map.entrySet()) {
            for (Entry<String, Integer> sub_entry : entry.getValue().entrySet()) {
                if (sub_entry.getValue() == 1) {
                    String word = sub_entry.getKey();
                    if (single_word_pos.containsKey(word)) {
                        single_word_pos.remove(word);
                    } else {
                        single_word_pos.put(word, entry.getKey());
                    }
                }
            }

        }
        HashMap<String, Double> single_word_pos_probs = new HashMap<>();
        for (String single_pos : single_word_pos.values()) {
            if (single_word_pos_probs.containsKey(single_pos)) {
                Double count = single_word_pos_probs.get(single_pos);
                count += 1.0;
                single_word_pos_probs.put(single_pos, count);
            } else {
                single_word_pos_probs.put(single_pos, 1.0);
            }
        }

        for (Entry<String, Double> entry : single_word_pos_probs.entrySet()) {
            single_word_pos_probs.put(entry.getKey(), entry.getValue() / (double)single_word_pos.size());
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
            ArrayList<String> observations = new ArrayList<>();
            String[] states = distinct_pos.toArray(new String[distinct_pos.size()]);

            while (testSc.hasNextLine()) {
                String word = testSc.nextLine();

                if (word.length() >= 1) {
                    observations.add(word);
                    continue;
                }
                //We're on an empty line, time to predict POS
                Double[][] viterbi_matrix = new Double[states.length][observations.size()];
                Integer[][] backpointer = new Integer[states.length][observations.size()];
                HashMap<String, Double> transition_prob_map = pos_unigram_probabilities;
                //initialization of matrices
                String first_word = observations.get(0);
                HashMap<String, Double> pos_prob_map = word_pos_probabilities.get(first_word);
                for (int i = 0; i < states.length; i++) {
                    String pos = states[i];
                    if (pos_prob_map != null) {
                        if (transition_prob_map.containsKey(pos) && pos_prob_map.containsKey(pos)) {
                            viterbi_matrix[i][0] = transition_prob_map.get(pos) * pos_prob_map.get(pos);
                        } else {
                            viterbi_matrix[i][0] = 0.0;
                        }
                    } else {
                        if (single_word_pos_probs.containsKey(pos)) {
                            if (transition_prob_map.containsKey(pos)) {
                                viterbi_matrix[i][0] = transition_prob_map.get(pos) * single_word_pos_probs.get(pos);
                            } else {
                                viterbi_matrix[i][0] = 0.0;
                            }
                        } else {
                            viterbi_matrix[i][0] = 0.0;
                        }

                        //if the word has a hyphen, it's probably an adjective
                        if (first_word.contains("-") && pos.equals("JJ")) {
                            viterbi_matrix[i][0] = 1.0;
                        }
                        //if the word is capitalized, it's probably a proper noun
                        if (!(first_word.equals(first_word.toLowerCase())) && pos.equals("NNP")) {
                            viterbi_matrix[i][0] = 1.0;
                        }
                    }
                    backpointer[i][0] = 0;
                }

                int j = 0;
                for (String curr_word : observations) {
                    if (j == 0) {
                        j++;
                        continue;
                    }
                    pos_prob_map = word_pos_probabilities.get(curr_word);
                    if (pos_prob_map != null) { //We have seen this word before
                        for (int i = 0; i < states.length; i++) {
                            String pos = states[i];
                            if (pos_prob_map.containsKey(pos)) { //we have seen this word as this pos
                                Double word_prob = pos_prob_map.get(pos);
                                Double max_prob = 0.0;
                                Integer max_pos = 0;
                                for (int k = 0; k < states.length; k++) {
                                    Double bigram_prob = 0.0;
                                    transition_prob_map = pos_bigram_probabilities.get(states[k]);
                                    if (transition_prob_map != null && transition_prob_map.containsKey(states[i])) {
                                        bigram_prob = transition_prob_map.get(states[i]);
                                    }
                                    Double curr_prob = bigram_prob * viterbi_matrix[k][j - 1];
                                    if (curr_prob > max_prob) {
                                        max_prob = curr_prob;
                                        max_pos = k;
                                    }
                                }
                                viterbi_matrix[i][j] = max_prob * word_prob;
                                backpointer[i][j] = max_pos;
                            } else { //we never saw this word as this pos
                                viterbi_matrix[i][j] = 0.0;
                                backpointer[i][j] = 0;
                            }
                        }
                    } else { //first time we have seen this
                        for (int i = 0; i < states.length; i++) {
                            String pos = states[i];
                            if (single_word_pos_probs.containsKey(pos)) {
                                Double single_word_prob = single_word_pos_probs.get(pos);
                                Double max_prob = 0.0;
                                Integer max_pos = 0;
                                for (int k = 0; k < states.length; k++) {
                                    Double bigram_prob = 0.0;
                                    transition_prob_map = pos_bigram_probabilities.get(states[k]);
                                    if (transition_prob_map != null && transition_prob_map.containsKey(states[i])) {
                                        bigram_prob = transition_prob_map.get(states[i]);
                                    }
                                    Double curr_prob = bigram_prob * viterbi_matrix[k][j - 1];
                                    if (curr_prob > max_prob) {
                                        max_prob = curr_prob;
                                        max_pos = k;
                                    }
                                }
                                //if the word has a hyphen, it's an adjective
                                if (curr_word.contains("-") && pos.equals("JJ")) {
                                    max_prob = 1.0;
                                }
                                //if the word is capitalized, it's probably a proper noun
                                if (!curr_word.equals(curr_word.toLowerCase()) && pos.equals("NNP")) {
                                    max_prob = 1.0;
                                }
                                viterbi_matrix[i][j] = max_prob * single_word_prob;
                                backpointer[i][j] = max_pos;
                            } else {
                                viterbi_matrix[i][j] = 0.0;
                                backpointer[i][j] = 0;
                            }
                        }
                    }
                    j++;
                }
                //now just follow back the backpointer
                Double max_final_pred = 0.0;
                int max_final_pos = 0;
                for (int l = states.length - 1; l >= 0; l--) {
                    if (viterbi_matrix[l][observations.size() - 1] > max_final_pred) {
                        max_final_pred = viterbi_matrix[l][observations.size() - 1];
                        max_final_pos = l;
                    }
                }
                Stack<String> pos_pred = new Stack<>();
                int m = observations.size() - 1;
                while (m >= 0) {
                    pos_pred.push(states[max_final_pos]);
                    max_final_pos = backpointer[max_final_pos][m];
                    m--;
                }
                for (String final_word : observations) {
                    out.write(final_word + "\t" + pos_pred.pop() + "\n");
                }
                out.write("\n");
                observations.clear();
            }

            if (testSc.ioException() != null) {
                System.out.print("Error while scanning");
                System.exit(1);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}