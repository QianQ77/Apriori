import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Itemset implements Comparable<Itemset>{
    private ArrayList<Integer> items;
    private int frequency;

    public Itemset(){
        items = new ArrayList<Integer>();
        frequency = 0;
    }

    public ArrayList<Integer> getItems() {
        return items;
    }

    public void setItems(ArrayList<Integer> items) {
        this.items = items;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Itemset(Integer item, int frequency){
        this.items = new ArrayList<Integer>();
        items.add(item);
        this.frequency = frequency;
    }

    public static Itemset join(Itemset set1, Itemset set2){
        Itemset result = new Itemset();
        result.items.addAll(set1.getItems());
        ArrayList<Integer> itemsOfSet2 = set2.getItems();
        result.items.add(itemsOfSet2.get(itemsOfSet2.size()-1));
        return result;
    }

    public static boolean canJoin(Itemset set1, Itemset set2){
        if(set1.items.size() != set2.items.size()){
            return false;
        }
        int i;
        for(i =0; i<set1.items.size()-1; i++){
            if(set1.items.get(i) != set2.items.get(i)){
                return false;
            }
        }
        if(set1.items.get(i).compareTo(set2.items.get(i))<0){
            return true;
        }else{
            return false;
        }
    }

    /*
        Being used in the Prune step;
        If one subset of this Itemset is not contained in previousFrequentItemsets,
        then this Itemset should not be a frequent itemset.
     */
    public boolean checkIfAllSubsetsContained(TreeSet<Itemset> previousFrequentItemsets){

        for(int i = 0; i< items.size(); i++){
            Integer itemAti = items.remove(i);
            if(!previousFrequentItemsets.contains(this)){
                items.add(i, itemAti);
                return false;
            }
            items.add(i, itemAti);
        }
        return true;
    }

    public int compareTo(Itemset another){
        if(another.items.size() != this.items.size())
            throw new IllegalStateException("These two itemset have different size");

        for(int i =0; i < this.items.size(); i++){
            int compare = this.items.get(i).compareTo(another.items.get(i));
            if(compare !=0){
                return compare;
            }
        }

        return 0;
    }

}
