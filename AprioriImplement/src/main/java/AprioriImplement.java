import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AprioriImplement {

    private HashMap<String, Integer> idMap;
    private HashMap<Integer, String> intToIdMap;
    private HashMap<Integer, BitSet> idIntBitSets;
    private int transactionCount;

    private int min_sup;
    private int k;
    private String dataFilePath;
    private String outFilePath;

    public AprioriImplement(int min_sup, int k, String dataFilePath, String outFilePath){
        this.min_sup = min_sup;
        this.k = k;
        this.dataFilePath = dataFilePath;
        this.outFilePath = outFilePath;
    }


    public void init(){
        try{
            List<String> lines = Files.readAllLines(Paths.get(dataFilePath));
            idMap = new HashMap<>();
            intToIdMap = new HashMap<>();
            idIntBitSets = new HashMap<>();
            int numberOfTransactions =0;
            Integer idInteger = 0;

            for(String line:lines){
                String[] ids = line.split("\\s+");
                BitSet bitset;

                //only keep the transactions which contain no less than k elements
                if(ids.length >= k){
                    for(String id:ids){
                        if(!idMap.containsKey(id)){
                            idMap.put(id, idInteger);
                            intToIdMap.put(idInteger, id);

                            bitset = new BitSet();
                            idIntBitSets.put(idInteger, bitset);

                            idInteger++;

                        }else{
                            bitset = idIntBitSets.get(idMap.get(id));
                        }

                        bitset.set(numberOfTransactions);

                    }
                    numberOfTransactions++;
                }

            }
            transactionCount = numberOfTransactions-1;

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

    /**
     * Firstly, count the frequency of all single items in all transitions;
     * Then, put items whose frequency are >= min_sup in to a TreeSet;
     */

    public TreeSet<Itemset> findFrequentOneItemsets(){
        TreeSet<Itemset> singleItemsets = new TreeSet<>();
        for(Integer idInt: idIntBitSets.keySet()){
            int frequency = idIntBitSets.get(idInt).cardinality();
            if(frequency >= min_sup){
                singleItemsets.add(new Itemset(idInt, frequency));
            }
        }
        return singleItemsets;
    }

    /**
     * This function has the same logic as findFrequentOneItemsets();
     * the only diffrent is that it uses paralleStream
     * @return
     */
    public TreeSet<Itemset> parallizedFindFrequentOneItemsets(){

         TreeSet<Itemset> singleItemsets = idIntBitSets.keySet().parallelStream()
                .collect(Collectors.toMap(idInt -> idInt, idInt ->idIntBitSets.get(idInt).cardinality()))
                .entrySet().parallelStream()
                .filter(e -> e.getValue()>=min_sup)
                .map(e -> new Itemset(e.getKey(), e.getValue()))
                .collect(Collectors.toCollection(TreeSet<Itemset>::new));

         return singleItemsets;
    }

    public int getItemsetFrequency(Itemset itemset){
        BitSet itemsetBitset = new BitSet(transactionCount);
        itemsetBitset.set(0, transactionCount, true);

        ArrayList<Integer> ids = itemset.getItems();
        for(Integer id:ids){
            itemsetBitset.and(idIntBitSets.get(id));
        }

        return itemsetBitset.cardinality();
    }


    /**
     *
     * @param previousFrequentItemsets Candidate K-1
     * @return
     */
    public TreeSet<Itemset> candidateGen(TreeSet<Itemset> previousFrequentItemsets){
        TreeSet<Itemset> CandidateK = new TreeSet<>();
        Iterator<Itemset> iterator = previousFrequentItemsets.iterator();
        while(iterator.hasNext()){
            Itemset set1 = iterator.next();
            //iterator2 start from the first itemset behind set1, end at the last itemset in previousFrequentItemsets;
            Iterator<Itemset> iterator2 = previousFrequentItemsets.tailSet(set1, false).iterator();
            while(iterator2.hasNext()){
                Itemset set2 = iterator2.next();
                if(Itemset.canJoin(set1, set2)){
                    Itemset joinSet = Itemset.join(set1, set2);
                    joinSet.setFrequency(getItemsetFrequency(joinSet));
                    if(joinSet.getFrequency() >= min_sup){
                        //prune the joinSet;
                        if(joinSet.checkIfAllSubsetsContained(previousFrequentItemsets)){
                            CandidateK.add(joinSet);
                        }
                    }
                }
            }
        }
        return CandidateK;
    }


    public ArrayList<TreeSet<Itemset>> findResultItemsets(){
        ArrayList<TreeSet<Itemset>> frequentItemsets = new ArrayList<>();

        //first pass
        //TreeSet<Itemset> singleFrequentItemsets = findFrequentOneItemsets();
        TreeSet<Itemset> singleFrequentItemsets = parallizedFindFrequentOneItemsets();
        TreeSet<Itemset> previous = singleFrequentItemsets;

        if(k == 1){
            frequentItemsets.add(singleFrequentItemsets);
        }

        //2nd, 3rd, .. pass until candidateGen() returns empty set.
        for(int i = 2; !previous.isEmpty(); i++){
            TreeSet<Itemset> next = candidateGen(previous);
            if(i >= k){
                frequentItemsets.add(next);
            }
            previous = next;
        }

        return frequentItemsets;
    }

    public void writeResultsToFile(ArrayList<TreeSet<Itemset>> results){
        Path outPath = Paths.get(outFilePath);
        ArrayList<String> lines = new ArrayList<>();

        for(TreeSet<Itemset> treeSet:results){
            for(Itemset itemset:treeSet){
                StringBuilder stringBuilder = new StringBuilder();

                for(Integer integer:itemset.getItems()){
                    String id = intToIdMap.get(integer);
                    stringBuilder.append(id+" ");
                }
                stringBuilder.append("("+itemset.getFrequency()+")");
                lines.add(stringBuilder.toString());
                //System.out.println(stringBuilder.toString());
            }
        }

        try{
            Files.write(outPath, lines, Charset.forName("UTF-8"));
        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

    public static void main(String[] args){
        int min_sup = new Integer(args[0]);
        int k = new Integer(args[1]);
        String dataFilePath = args[2];
        String outFilePath = args[3];

        /*System.out.println("Please input min_sup:");
        Scanner scanner = new Scanner(System.in);
        int min_sup = new Integer(scanner.next());
        System.out.println("Please input k:");
        int k = new Integer(scanner.next());

        String dataFilePath = "transactionDB.txt";
        String outFilePath = "new-output_minsup"+min_sup+"k"+k+"+.txt";*/


        long startTime = System.currentTimeMillis();
        AprioriImplement apriori = new AprioriImplement(min_sup, k, dataFilePath, outFilePath);
        apriori.init();
        apriori.writeResultsToFile(apriori.findResultItemsets());
        long endTime = System.currentTimeMillis();
        System.out.println("Total Time Cost:"+(endTime-startTime)/1000+" seconds");

    }
}
