/**
 * @(#)shopify.java
 *
 * shopify application
 *
 * @Atilla Saadat
 * @version 1.00 2016/1/19
 *
 * Library imported from JSON jar found here: http://central.maven.org/maven2/org/json/json/20151123/json-20151123.jar
 */
 
import java.util.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.*;

public class shopify {

  //---------------------------------------------------------------------------------------------------
  // JSON Parse and read funtions
  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }
  //---------------------------------------------------------------------------------------------------
  //Converts ArrayList<Integer> to int[] Array
  public static int[] convertIntegers(ArrayList<Integer> myList)
	{
    int[] arr = new int[myList.size()];
		for(int i = 0; i < myList.size(); i++) {
    		if (myList.get(i) != null) {
        		arr[i] = myList.get(i);
    		}
		}
		return arr;
	}
  //---------------------------------------------------------------------------------------------------
  // 1/0 Greedy (knapsack) algorithm for determining which items to take or leave behind
  // One per item as based on intern email
  public static double finalPrice (int maxWeight, ArrayList<Double> prices, ArrayList<Integer> weights){
  	int[]weight = convertIntegers(weights);
  	int[]values = new int[weights.size()];
  	
    //Set all values as one because price is not a deciding factor in choice
    Arrays.fill(values,1);
  	
  	int N = values.length-1;
  	int W = maxWeight;
  	
  	int[][] opt = new int[N+1][W+1];
    boolean[][] sol = new boolean[N+1][W+1];

    for (int n = 1; n <= N; n++) {
    	for (int w = 1; w <= W; w++) {
            // don't take item n
            int option1 = opt[n-1][w];
            // take item n
            int option2 = Integer.MIN_VALUE;
            if (weight[n] <= w){
            	option2 = values[n] + opt[n-1][w-weight[n]];
            }
			// select better of two options
            opt[n][w] = Math.max(option1, option2);
            sol[n][w] = (option2 > option1);
        }
    }

    // determine which items to take
    boolean[] take = new boolean[N+1];
    for (int n = N, w = W; n > 0; n--) {
        if (sol[n][w]){ 
        	take[n] = true;  
        	w = w - weight[n];
        }
        else{
        	take[n] = false;
        }
    }
    
    double totalPrice = 0;
    
    for (int n = 1; n <= N; n++) {
    	if(take[n]){
    		totalPrice+=prices.get(n-1);
    	}
    }
    return totalPrice;
  }
  //---------------------------------------------------------------------------------------------------
  // Gets Price and Weights of Keyboard and Computer Variants 
  public static double findKeyCompTotalPrice(int maxWeight, String jsonLink) throws IOException, JSONException{
    JSONArray products = (readJsonFromUrl(jsonLink)).getJSONArray("products");
    ArrayList<Double> priceList = new ArrayList<Double>();
    ArrayList<Integer> weightList = new ArrayList<Integer>();
    for(int index = 0; index < products.length(); index++){
      String currentItem = products.getJSONObject(index).getString("product_type");
      if(currentItem.equals("Keyboard")||currentItem.equals("Computer")){ // Search for Keyboard or Computer
        int numOfVariants = products.getJSONObject(index).getJSONArray("variants").length();
        for(int variant = 0; variant < numOfVariants; variant++){
          priceList.add(Double.parseDouble(products.getJSONObject(index).getJSONArray("variants").getJSONObject(variant).getString("price")));
          weightList.add(products.getJSONObject(index).getJSONArray("variants").getJSONObject(variant).getInt("grams"));
        }
      }
    }
    // Return Price of all Keyboard and Computer Variants based of 1/0 Greedy Algorithm
    return finalPrice(maxWeight,priceList,weightList);
  }
  //---------------------------------------------------------------------------------------------------
  public static void main(String[] args) throws IOException {
    int maxWeight = 100000;
    String jsonLink = "http://shopicruit.myshopify.com/products.json";
    
    //Based on above link, all items should be taken because: all Keyboard and Computer weights < 100kg
    System.out.println(findKeyCompTotalPrice(maxWeight,jsonLink));
  }
}