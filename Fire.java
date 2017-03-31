import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Fire extends JFrame{
    private static final boolean GRAPHIC=true;
    private static final char BURNING = 'w'; //w looks like fire, right?
    private static final char TREE = 'T';
    private static final char FIGHTER1='F';
    private static final char FIGHTER2='f';
    private static final char EMPTY = '.';
    private static final double F = .001   ;
    private static final double P = .01;
    private static final double TREE_PROB = 0;
    private static final int FIRE_FIGHTERS=10;
    private List<String> land;
    private JPanel landPanel;

    public Fire(List<String> land){
        this.land = land;
        if(GRAPHIC) {
            landPanel = new JPanel() {
                @Override
                public void paint(Graphics g) {
                    for (int y = 0; y < Fire.this.land.size(); y++) {
                        String row = Fire.this.land.get(y);
                        for (int x = 0; x < row.length(); x++) {
                            switch (row.charAt(x)) {
                                case BURNING:
                                    g.setColor(Color.RED);
                                    break;
                                case TREE:
                                    g.setColor(Color.GREEN);
                                    break;
                                case FIGHTER1:
                                case FIGHTER2:
                                    g.setColor(Color.BLUE);
                                    break;
                                default: //will catch EMPTY
                                    g.setColor(Color.WHITE);
                            }
                            g.fillRect(x * 3, y * 3, 3, 3);
                        }
                    }
                }
            };
            //each block in the land is a 3x3 square
            landPanel.setSize(this.land.get(0).length() * 3, this.land.size() * 3);
            add(landPanel);
            setSize(1000, 1000);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }

    private List<String> process(List<String> land){
        land=fireFighterToTheRescue(land);
        LinkedList<String> newLand = new LinkedList<String>();
        for(int i = 0; i < land.size(); i++){
            String rowAbove, thisRow = land.get(i), rowBelow;
            if(i == 0){//first row
                rowAbove = null;
                rowBelow = land.get(i + 1);
            }else if(i == land.size() - 1){//last row
                rowBelow = null;
                rowAbove = land.get(i - 1);
            }else{//middle
                rowBelow = land.get(i + 1);
                rowAbove = land.get(i - 1);
            }
            newLand.add(processRows(rowAbove, thisRow, rowBelow));
        }
        return newLand;
    }

    private String processRows(String rowAbove, String thisRow,
                               String rowBelow){
        String newRow = "";
        for(int i = 0; i < thisRow.length();i++){
            switch(thisRow.charAt(i)){
                case BURNING:
                    newRow+= EMPTY;
                    break;
                case EMPTY:
                    newRow+= Math.random() < P ? TREE : EMPTY;
                    break;
                case TREE:
                    String neighbors = "";
                    if(i == 0){//first char
                        neighbors+= rowAbove == null ? "" : rowAbove.substring(i, i + 2);
                        neighbors+= thisRow.charAt(i + 1);
                        neighbors+= rowBelow == null ? "" : rowBelow.substring(i, i + 2);
                        if(neighbors.contains(Character.toString(BURNING))){
                            newRow+= BURNING;
                            break;
                        }
                    }else if(i == thisRow.length() - 1){//last char
                        neighbors+= rowAbove == null ? "" : rowAbove.substring(i - 1, i + 1);
                        neighbors+= thisRow.charAt(i - 1);
                        neighbors+= rowBelow == null ? "" : rowBelow.substring(i - 1, i + 1);
                        if(neighbors.contains(Character.toString(BURNING))){
                            newRow+= BURNING;
                            break;
                        }
                    }else{//middle
                        neighbors+= rowAbove == null ? "" : rowAbove.substring(i - 1, i + 2);
                        neighbors+= thisRow.charAt(i + 1);
                        neighbors+= thisRow.charAt(i - 1);
                        neighbors+= rowBelow == null ? "" : rowBelow.substring(i - 1, i + 2);
                        if(neighbors.contains(Character.toString(BURNING))){
                            newRow+= BURNING;
                            break;
                        }
                    }
                    newRow+= Math.random() < F ? BURNING : TREE;
                    break;

                case(FIGHTER2):
                    newRow+= TREE;
                    break;
                case(FIGHTER1):
                    newRow+=FIGHTER2;
                    break;
            }
        }
        return newRow;
    }
    Random rand=new Random();

    public List<String> fireFighterToTheRescue(List<String> newLand){
        Point index=new Point(-1,-1);
        ArrayList<Point> burningIndexes=new ArrayList<>();
        int n=0;
        for (int i = 0; i <newLand.size(); i++) {
            for (int j = 0; j <newLand.get(i).length() ; j++) {
                if(newLand.get(i).charAt(j)==BURNING) burningIndexes.add(new Point(i,j));
            }
        }
        StringBuilder newString;
        while(burningIndexes.size()>0&&n<FIRE_FIGHTERS){
            n++;
            index=burningIndexes.get(rand.nextInt(burningIndexes.size()));
            newString=new StringBuilder(newLand.get(index.x));
            newString.setCharAt(index.y,FIGHTER1);
            newLand.set(index.x,newString.toString());
        }
        return newLand;
    }



    public static List<String> populate(int width, int height){
        List<String> land = new LinkedList<String>();
        for(;height > 0; height--){//height is just a copy anyway
            StringBuilder line = new StringBuilder(width);
            for(int i = width; i > 0; i--){
                line.append((Math.random() < TREE_PROB) ? TREE : EMPTY);
            }
            land.add(line.toString());
        }
        return land;
    }

    public double processN(int n) {
        for(int i = 0;i < n; i++){
            land = process(land);
            updateBiomass(land);
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            if(GRAPHIC)repaint();
        }
        return (double)biomass/n;
    }

    int biomass=0;
    public void updateBiomass(List<String> land){
       int temp=0;
        for (int i = 0; i <land.size(); i++) {
            for (int j = 0; j <land.get(i).length() ; j++) {
                if(land.get(i).charAt(j)==TREE) temp++;
            }
        }
        biomass+=temp;
    }

    public int fitnessOfTheForsest(List<String> land){
        int fitness=-1;

        return fitness;
    }
    public static void main(String[] args){
        List<String> land;

        land = populate(256, 256);
        Fire fire2 = new Fire(land);
        double biomass = fire2.processN(5000);
        System.out.println(biomass);
    }
}