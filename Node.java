public class Node {
    private int frequency; //stores the frequency of the character
    private char character; //stores the character itself

    public Node(char letter, int freq){
        character = letter;
        frequency = freq;
    }

    public Node(){
        character = 0;
        frequency = 0;
    }

    public int getFrequency(){
        return frequency;
    }

    public char getCharacter(){
        return character;
    }

    public void setCharacter(char letter){
        character = letter;
    }

    public void setFrequency(int freq){
        frequency = freq;
    }

    @Override
    public String toString(){
        String str = "c" + character + "f" + frequency;
        return str;
    }
}
