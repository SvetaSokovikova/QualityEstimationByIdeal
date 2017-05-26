package com.mycompany.qualityestimationbyideal;

import java.io.File;
import java.io.FileWriter;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

public class Main {
    
    public static void main(String[] args){
        
        String database_name = "C:/Films";  //Database name parameter
        String resultFileName = "C:/Users/User/Desktop/FilmsEstimation.txt"; // Result file name parameter
        
        GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
        File location = new File(database_name);
        GraphDatabaseService graphDb = 
                dbFactory.newEmbeddedDatabase(location);
        
        Map<String,Object> n_type = graphDb.execute("match (n) return count(distinct n.type) as n").next();
        Map<String,Object> n_typeideal = graphDb.execute("match (n) return count(distinct n.type) as n").next();
        
        long n_clusters = (long) n_type.get("n");
        
        if (n_clusters != (long)n_typeideal.get("n")){
            System.out.println("Ошибка! Количество типов должно совпадать с количеством идеальных типов!");
            return;
        }
        
        long[][] table = new long[(int)n_clusters][(int)n_clusters];
        
        long N = 0;
        
        for (int i=0;i<n_clusters;i++)
            for (int j=0;j<n_clusters;j++)
                table[i][j] = 0;
        
        try (Transaction tx = graphDb.beginTx()){
            ResourceIterable<Node> all_nodes = graphDb.getAllNodes();
            
            for (Node n: all_nodes){
                long i = (long)n.getProperty("type") ;
                long j = (long)n.getProperty("type_ideal");
                table[(int)i-1][(int)j-1]++;
                        
                N++;
            }
       
            tx.success();
        }
        
        long[] cluster = new long[(int)n_clusters];
        long[] klass = new long[(int)n_clusters];
        
        for (int i=0;i<n_clusters;i++){
            cluster[i] = 0;
            klass[i] = 0;
        }
        
        for (int j=0;j<n_clusters;j++)
            for (int i=0;i<n_clusters;i++)
                cluster[j]+=table[i][j];
        
        for (int i=0;i<n_clusters;i++)
            for (int j=0;j<n_clusters;j++)
                klass[i]+=table[i][j];
        
        long SS = 0;
        long SD = 0;
        long DS = 0;
        long DD = 0;
        
        long buf;
        
        for (int i=0;i<n_clusters;i++)
            for (int j=0;j<n_clusters;j++){
                buf = table[i][j];
                SS += buf *(buf - 1)/2;
                SD += buf *(cluster[j]-buf);
                DS += buf *(klass[i]-buf);
            }
        
        DD = N*(N-1)/2 - SS - SD - DS;
        
        double Rand;
        double Jaccard;
        double FM;
        
        Rand = (double)(SS + DD)/(double)(SS + DS + SD + DD);
        Jaccard = (double)SS / (double)(SS + SD + DS);
        FM = (double)SS / Math.sqrt((double)(SS + SD) * (double)(SS + DS));
        
        File res = new File(resultFileName);
        try{
            FileWriter fw = new FileWriter(res,false);
            
            fw.append(" | ");
            for (int i=1;i<=n_clusters;i++)
                fw.append(i + "  ");
            fw.append("\n");
            
            fw.append("-|-");
            for (int i=1;i<=n_clusters;i++)
                fw.append("---");
            fw.append("\n");
            
            for (int i=0;i<n_clusters;i++){
                fw.append(String.valueOf(i+1)+"|  ");
                for (int j=0;j<n_clusters;j++)
                    fw.append(table[i][j] + "  ");
                fw.append("\n");
            }
            
            fw.append("\n");
            
            fw.append("Rand statistic = "+Rand+"\n");
            fw.append("Jaccard index = "+Jaccard+"\n");
            fw.append("Folkes and Mallows index = "+FM+"\n");
            fw.close();
        }
        catch (Exception e){
            System.out.println("Не удалось записать результат в файл!");
        }
                
    }
    
}
