import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * @author: Akshee Chopra
 * @author: Daisy Li
 * PS-3: Compression Driver with frequency table, priority queue, huffree, codeMap
 */


public class CompressionDriver {

    public static void main(String[] args) throws IOException {

        String wAndP = "WarAndPeace.txt";
        String con = "USConstitution.txt";
        String emptyF = "Empty"; //test an empty file
        String oneChar = "OneCharacter"; //test a file with only one character
        String repChar = "RepeatedCharacter"; //test a file with a single character repeated
        String hello = "Hello"; // small tester file with a couple of words
        String inputPath = "inputs/" + hello;
        TreeMap<Character, Integer> freqtable = freqTable(inputPath);
        PriorityQueue<BinaryTree<Node>> pq = priorityQueue(freqtable);
        BinaryTree<Node> huffTree = huffTree(pq, freqtable);
        HashMap<Character, String> codeMap = codeRetrieval(huffTree);
        compress(inputPath, codeMap);
        decompress(inputPath + "_compressed.txt", huffTree);
    }

    /**
     * @param fileName
     * @return a TreeMap which stores the character and its frequency
     * @throws IOException
     */
    public static TreeMap<Character, Integer> freqTable(String fileName) throws IOException {
        TreeMap<Character, Integer> freqTable = new TreeMap<>();
        try {
            BufferedReader input = new BufferedReader(new FileReader(fileName));

            //iterate through each character in the file
            int in = input.read();
            while (in != -1) {
                char ch = (char) in; //cast in to a char

                if (freqTable.containsKey(ch) == false) { //add character to freqTable if it isn't there already
                    freqTable.put(ch, 1);
                } else {
                    freqTable.replace(ch, freqTable.get(ch) + 1); //else, just increment the frequency of the character
                }

                in = input.read(); //keep progressing through the document
            }
        }

        catch(FileNotFoundException e){
            throw new IOException("File not found");
        }

        return freqTable;

    }

    /**
     * @param freqTable
     * @return a PriorityQueue of initial single-character trees sorted by frequency
     */
    public static PriorityQueue<BinaryTree<Node>> priorityQueue(TreeMap<Character, Integer> freqTable){

        //create PriorityQueue with anonymous function to compare the frequencies of nodes
        PriorityQueue<BinaryTree<Node>> pq = new PriorityQueue<BinaryTree<Node>>((BinaryTree<Node> n1, BinaryTree<Node> n2) -> n1.getData().getFrequency() - n2.getData().getFrequency());
        return pq;
    }

    /**
     * @param pq
     * @param freqTable
     * @return a Binary Tree sorted by Huffman encoding
     */
    public static BinaryTree<Node> huffTree(PriorityQueue<BinaryTree<Node>> pq, TreeMap<Character, Integer> freqTable){
        //iterate thru each character in the freq table
        for(Character c: freqTable.keySet()){
            Node n = new Node(c, freqTable.get(c)); //create a Node with the character and its frequency
            BinaryTree<Node> tree = new BinaryTree<>(n); //create a new tree that holds new Node n
            pq.add(tree);
        }


        for (int i = pq.size()-1; i>0; i--){ //iterate through the pq to create one Huffman encoded tree

            BinaryTree<Node> lf = pq.poll(); // removes lowest freq tree (T1)
            BinaryTree<Node> nlf = pq.poll(); // removes next-lowest freq tree (T2)

            Integer freq = lf.getData().getFrequency() + nlf.getData().getFrequency(); //total frequency of T1 + T2
            //create a new tree with lf and nlf as the subtrees and the total frequency in the node
            BinaryTree<Node> T = new BinaryTree<Node>(new Node('#', freq), lf, nlf);
            //use character # to represent internal nodes

            pq.add(T);
        }

        return pq.peek(); //returns the Huffman encoded tree
    }

    /**
     * @param huffTree
     * @return a HashMap that stores codes for each character
     */
    public static HashMap<Character, String> codeRetrieval(BinaryTree<Node> huffTree){
        //make a map that pairs characters with their code words
        HashMap<java.lang.Character, java.lang.String> map = new HashMap<java.lang.Character, java.lang.String>();
        retrievalHelper(huffTree, map, ""); //use recursion to traverse huffTree
        return map;
    }


    /**
     * @param huffTree BinaryNode
     * @param codeMap
     * @param psf
     */
    public static void retrievalHelper(BinaryTree<Node> huffTree, HashMap<Character, String> codeMap, String psf){
        //construct map through an entire traversal of the tree
        //keep track of a "path so far" parameter (psf) as you do the traversal
        if(huffTree!=null && huffTree.isLeaf()){
            if(psf.equals("")){ //if there is only 1 character in the file
                psf = "0"; //character code defaults to 0
                codeMap.put(huffTree.data.getCharacter(), psf) ;
            }
            codeMap.put(huffTree.data.getCharacter(), psf);
        }
        if (huffTree!=null && huffTree.hasRight()) { //if character is a right child
            retrievalHelper(huffTree.getRight(), codeMap, psf + '1'); //add 1 to character's code
        }

        if (huffTree!=null && huffTree.hasLeft()) { //if character is a left child
            retrievalHelper(huffTree.getLeft(), codeMap, psf + '0'); //ad 0 to character's code
        }
    }

    /**
     * @param input
     * @param codeMap
     * @throws IOException
     */
    public static void compress(String input, HashMap<Character, String> codeMap) throws IOException {

        //TODO: DO TRY CATCH

        // input: inputs/USConstitution.txt
        // input: inputs/Hello

        int dot = input.indexOf(".");
        String pathName;

        if(dot!=-1){ //if file name ends with .txt
            pathName = input.substring(0,dot) + "_compressed.txt"; //cut off ".txt", then concatenate "_compressed.txt"
        }

        else{ //if file name doesn't have an ending
            pathName = input + "_compressed.txt";  //concatenate "_compressed.txt"
        }
        try {
            BufferedBitWriter compressed = new BufferedBitWriter(pathName); // inputs/USConstitution_compressed.txt, inputs/Hello_compressed.txt
            BufferedReader reading = new BufferedReader(new FileReader(input)); // inputs/USConstitution.txt, inputs/Hello

            //iterate thru each character in the file
            int in = reading.read();
            while (in != -1) {
                char ch = (char) in;

                if (codeMap.containsKey(ch)) { //looks up character in codeMap
                    String encoded = codeMap.get(ch); //gets character's code
                    for (int i = 0; i < encoded.length(); i++) { //iterates thru each character in character's code
                        if (encoded.substring(i, i + 1).equals("0")) { //if character is a 0
                            compressed.writeBit(false); //write false to compressed file
                        } else { //if character is a 1
                            compressed.writeBit(true); //write true to compressed file
                        }
                    }
                }
                in = reading.read();
            }
            compressed.close();
            reading.close();
        }
        catch (FileNotFoundException e) {
            throw new IOException("File not found");
        }
    }

    /**
     * @param inputPath
     * @param bigTree
     * @throws IOException
     */
    public static void decompress(String inputPath, BinaryTree<Node> bigTree) throws IOException {

        //inputPath = inputs/USConstitution.txt_compressed.txt
        //inputPath = inputs/Hello_compressed.txt

        int dash = inputPath.indexOf("_");
        int dot = inputPath.indexOf(".");

        String pathName; //path of decompressed file

        if(dot < dash){ // if file name contains a dot before a dash (".txt_compressed.txt")
            pathName = inputPath.substring(0, dot) + "_decompressed.txt"; //cut off the ".txt_compressed.txt" and replace with "_decompressed.txt"
            inputPath = inputPath.substring(0, dot) + "_compressed.txt"; //cut off the ".txt_compressed.txt" and replace with "_compressed.txt"
        }

        else{
            pathName = inputPath.substring(0, dash) + "_decompressed.txt";
        }
        try {
            BufferedWriter decompressed = new BufferedWriter(new FileWriter(pathName));  // inputs/USConstitution_decompressed.txt, inputs/Hello_decompressed.txt
            BufferedBitReader compressed = new BufferedBitReader(inputPath); // inputs/USConstitution_compressed.txt, inputs/Hello_compressed.txt


            boolean bit;
            BinaryTree<Node> newTree = bigTree; //assign newTree to root of bigTree
            while (compressed.hasNext() && newTree.getData() != null) { //iterate thru bits in compressed file
                bit = compressed.readBit();

                if (bit == true && newTree.getRight() != null) { //if bit == true (1)
                    newTree = newTree.getRight(); //reassign newTree to newTree's right child
                }

                if (bit == false && newTree.getLeft() != null) { //if bit == false (0)
                    newTree = newTree.getLeft(); //reassign newTree to newTree's left child
                }

                if (newTree.getData() != null && newTree.isLeaf()) { //when we get to a leaf node
                    decompressed.write(newTree.getData().getCharacter()); //write the character in the node to decompressed file
                    newTree = bigTree; //reset newTree to root of bigTree
                }
            }
            decompressed.close();
        }
        catch (FileNotFoundException e) {
            throw new IOException("File not found");
        }
    }
}

























//
///*
//- do try catch in compress and decompress and when ur making freqtable
//- java docs
// */
//
///**
// * Compression-Driver for PS3
// * Main method calls all the methods for making thr frequency table, making the PQ, making the HuffTree,
// * and then compressing and decompressing the file
//
// * @author Akshee Chopra and Daisy Li
// */
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.PriorityQueue;
//import java.util.TreeMap;
//
//public class CompressionDriver {
//
//    public static void main(String[] args) throws IOException {
//            String wAndP = "WarAndPeace.txt";
//            String con = "USConstitution.txt";
//            String emptyF = "Empty"; //test an empty file
//            String oneChar = "OneCharacter"; //test a file with only one character
//            String repChar = "RepeatedCharacter"; //test a file with a single character repeated
//            String hello = "Hello"; // small tester file with a couple of words
//            String pathName = "inputs/" + con;
//            TreeMap<Character, Integer> freqtable = freqTable(pathName);
//            PriorityQueue<BinaryTree<Node>> pq = priorityQueue(freqtable);
//            BinaryTree<Node> huffTree = huffTree(pq, freqtable);
//            HashMap<Character, String> codeMap = traversal(huffTree);
//            System.out.println(codeMap);
//            compress(pathName, codeMap);
//            decompress(pathName+"_compressed.txt", huffTree);
//
//
//
//    }
//
//    public static TreeMap<Character, Integer> freqTable(String fileName) throws IOException {
//        TreeMap<Character, Integer> freqtable = new TreeMap<>();
//        BufferedReader input = new BufferedReader(new FileReader(fileName));
//
//        //iterate through each character in the file
//        int in = input.read();
//        while (in != -1) {
//            char ch = (char) in;
//
//            if (freqtable.containsKey(ch) == false) { //add character to freqtable if it isn't there already
//                freqtable.put(ch, 1);
//            } else {
//                freqtable.replace(ch, freqtable.get(ch) + 1); //else, just increment the frequency of the character
//            }
//
//            in = input.read(); //keep progressing through the document
//        }
//
//        return freqtable;
//
//    }
//
//    public static PriorityQueue<BinaryTree<Node>> priorityQueue(TreeMap<Character, Integer> freqtable){
//
//        //create PriorityQueue with anonymous function to compare the frequencies of nodes
//        PriorityQueue<BinaryTree<Node>> pq = new PriorityQueue<BinaryTree<Node>>((BinaryTree<Node> n1, BinaryTree<Node> n2) -> n1.getData().getFrequency() - n2.getData().getFrequency());
//        return pq;
//    }
//
//    public static BinaryTree<Node> huffTree(PriorityQueue<BinaryTree<Node>> pq, TreeMap<Character, Integer> freqtable){
//        for(Character c: freqtable.keySet()){ //adding trees to the PQ
//            Node n = new Node(c, freqtable.get(c));
//            BinaryTree<Node> tree = new BinaryTree<>(n); //trees that hold a node with a character and its frequency
//            pq.add(tree);
//        }
//
//
//        for (int i = pq.size()-1; i>0; i--){ //iterate through the pq to create one big tree
//
//            BinaryTree<Node> lf = pq.poll(); // removes lowest freq tree (T1)
//            BinaryTree<Node> nlf = pq.poll(); // removes next-lowest freq tree (T2)
//
//            Integer freq = lf.getData().getFrequency() + nlf.getData().getFrequency(); //total frequency of T1 + T2
//
//            BinaryTree<Node> T = new BinaryTree<Node>(new Node('#', freq), lf, nlf); //create a new tree with lf and nlf as the subtrees and the total frequency in the node
//
//            pq.add(T);
//        }
//
//        return pq.peek();
//    }
//
//    public static HashMap<Character, String> traversal(BinaryTree<Node> huffTree){
//        //make a map that pairs characters with their code words
//        HashMap<java.lang.Character, java.lang.String> map = new HashMap<java.lang.Character, java.lang.String>();
//        travHelper(huffTree, map, "");
//        return map;
//    }
//
//
//    public static void travHelper(BinaryTree<Node> tr, HashMap<Character, String> map, String psf){
//        //construct map through an entire traversal of the tree
//        //keep track of a "path so far" parameter as u do the traversal
//            if(tr!=null && tr.isLeaf()){
//                if(psf.equals("")){
//                    psf = "0";
//                    map.put(tr.data.getCharacter(), psf) ;
//                }
//                map.put(tr.data.getCharacter(), psf);
//            }
//            if (tr!=null && tr.hasRight()) {
//                travHelper(tr.getRight(), map, psf + '1');
//            }
//
//            if (tr!=null && tr.hasLeft()) {
//                travHelper(tr.getLeft(), map, psf + '0');
//            }
//    }
//
//    public static void compress(String input, HashMap<Character, String> map) throws IOException {
//
//        //TODO: DO TRY CATCH
//
//        // input: inputs/USConstitution.txt
//        // input: inputs/Hello
//
//        int dot = input.indexOf(".");
//        String pathName;
//
//        if(dot!=-1){ //if it has .txt
//            pathName = input.substring(0,dot) + "_compressed.txt"; // inputs/USConstitution_compressed.txt
//        }
//
//        else{
//            pathName = input + "_compressed.txt";  // inputs/Hello_compressed.txt
//        }
//
//        BufferedBitWriter output = new BufferedBitWriter(pathName); // inputs/USConstitution_compressed.txt, inputs/Hello_compressed.txt
//
//
//        BufferedReader reading = new BufferedReader(new FileReader(input)); // inputs/USConstitution.txt, inputs/Hello
//
//
//        int in = reading.read();
//        while (in != -1) {
//            char ch = (char) in;
//
//            if (map.containsKey(ch)){
//                String encoded = map.get(ch);
//                for(int i = 0; i< encoded.length(); i++){
//                    if(encoded.substring(i, i+1).equals("0")){
//                        output.writeBit(false);
//                        }
//                    else{
//                        output.writeBit(true);
//                    }
//
//                }
//            }
//            in = reading.read();
//        }
//        output.close();
//        reading.close();
//    }
//
//    public static void decompress(String inputPath, BinaryTree<Node> bigTree) throws IOException {
//
//        //TODO: DO TRY CATCH
//
//        //inputPath = inputs/USConstitution.txt_compressed.txt
//        //inputPath = inputs/Hello_compressed.txt
//
//        int dash = inputPath.indexOf("_");
//        int dot = inputPath.indexOf(".");
//
//        String pathName;
//
//        if(dot < dash){ // if it has .txt_compressed.txt
//            pathName = inputPath.substring(0, dot) + "_decompressed.txt";
//            inputPath = inputPath.substring(0, dot) + "_compressed.txt";
//        }
//
//        else{
//            pathName = inputPath.substring(0, dash) + "_decompressed.txt";
//        }
//
//        BufferedWriter rf = new BufferedWriter(new FileWriter(pathName));  // inputs/USConstitution_decompressed.txt, inputs/Hello_decompressed.txt
//
//        BufferedBitReader br = new BufferedBitReader(inputPath); // inputs/USConstitution_compressed.txt, inputs/Hello_compressed.txt
//
//
//        boolean bit;
//        BinaryTree<Node> newTree = bigTree;
//        while(br.hasNext() && newTree.getData()!=null){
//            bit = br.readBit();
//
//
//            if(bit == true && newTree.getRight()!=null){
//                newTree = newTree.getRight();
//            }
//
//            if(bit == false && newTree.getLeft()!=null){
//                newTree = newTree.getLeft();
//            }
//
//            if(newTree.getData() !=null && newTree.isLeaf()){
//                rf.write(newTree.getData().getCharacter());
//                newTree = bigTree;
//
//            }
//
//        }
//
//        rf.close();
//
//    }
//
//}
