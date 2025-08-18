package Project.Finance_News.service.quiz;

public class CrosswordItem {
    public int row;
    public int col;
    public int length;
    public String direction; // "across" 또는 "down"
    public String term;
    public String clue;
    public int number;
    public CrosswordItem(int row, int col, int length, String direction, String term, String clue, int number) {
        this.row = row; this.col = col; this.length = length; this.direction = direction;
        this.term = term; this.clue = clue; this.number = number;
    }
} 