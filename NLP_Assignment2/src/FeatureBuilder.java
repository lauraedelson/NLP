import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import opennlp.maxent.io.*;


public class FeatureBuilder {
    public static void main(String[] args) {
        String START = "start";
        String END = "end";

        if (args.length < 2) {
            System.out.print("Usage: FeatureBuilder token_file.txt output_file.txt");
            System.exit(1);
        }

        String training_path = args[0];
        String output_path = args[1];
        HashMap<String, Integer> pos_count_map = new HashMap<> ();
        HashMap<String, HashMap<String, Integer>> pos_chunk_map = new HashMap<>();
        ArrayList<String> file = new ArrayList<>();
        ArrayList<String> next_pos = new ArrayList<>();

        //read training file
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(training_path);
        } catch (FileNotFoundException e) {
            System.out.print("Input file not found");
            System.exit(1);
        }
        sc = new Scanner(inputStream, "UTF-8");
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            file.add(line);
            String[] parts = line.split("\t");
            if (parts.length > 1) {
                String word = parts[0];
                String POS = parts[1];
                next_pos.add(POS);
            }
        }

        next_pos.remove(0);
        next_pos.add(END);

        FileOutputStream output_stream = null;
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(output_path));
            String prev_pos = START;
            String prev_token = "";
            String prev_chunk = "O";
            int i = 0;
            for (String line: file) {
                String[] parts = line.split("\t");
                if (parts.length > 1) {

                    String word = parts[0];
                    String POS = parts[1];
                    output.write(word + "\t");
                    output.write("prev_token=" + prev_token + "\t");
                    output.write("prev_pos=" + prev_pos + "\t");
                    output.write("curr_pos=" + POS + "\t");
                    output.write("next_pos=" + next_pos.get(i) + "\t");
                    if (parts.length > 2) {
                        String chunk = parts[2];
                        output.write("prev_chunk=" + prev_chunk + "\t");
                        output.write(chunk);
                        prev_chunk = chunk;
                    } else {
                        output.write("prev_chunk=@@");
                    }
                    output.write("\n");
                    prev_pos = POS;
                    prev_token = word;
                    i++;
                } else {
                    output.write(line + "\n");
                }
            }
            output.close();
        }catch (IOException ie) {
            System.out.print("Output file could not be written");
            System.exit(1);
        }
    }
}