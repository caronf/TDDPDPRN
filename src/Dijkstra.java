//import java.util.ArrayList;
//
/////**
//// *
//// * @author Maha G.
//// */
//public class Dijkstra {
////
////
////    static float dijkstraArrivalTime(int sourceIndex,int targetIndex, float departureTime)
////    {
////
////
////
////        float dist[] = new float[InputData.distanceMatrix.length];
////
////
////	Boolean sptSet[] = new Boolean[InputData.distanceMatrix.length];
////
////
////	for (int i = 0; i < InputData.distanceMatrix.length; i++)
////
////	  {
////	      dist[i] = Integer.MAX_VALUE;
////	      sptSet[i] = false;
////	  }
////
////
////	dist[sourceIndex] = departureTime;
////
////
////	for (int count = 0; count < InputData.distanceMatrix.length -1; count++)
////	  {
////
////	      if(sptSet[targetIndex] )break;
////	      int u = minDistance(dist, sptSet);
////
////	      sptSet[u] = true;
////
////	      for (int v = 0; v < InputData.distanceMatrix.length; v++){
////
////		  if(!sptSet[v] && InputData.distanceMatrix[u][v]!=0){
////
////
////		      float distUV = travelTimeCalculate(InputData.distanceMatrix[u][v],dist[u],InputData.speedFonctionList.get(InputData.speedFunctionMatrix[u][v]-1));
////		      if (dist[u]+distUV < dist[v]){
////			  dist[v] = distUV;
////		      }
////		  }}
////
////	  }
////
////	return dist[targetIndex];
////
////
////    }
////
////
////
//    static ArrayList<String> dijkstraPath(int departureNode, float departureTime, InputData inputData)
//    {
//        double[] arrivalTimes = new double[inputData.distanceMatrix.length];
//	    boolean[] sptSet = new boolean[inputData.distanceMatrix.length];
//        int[] previousNodes = new int[inputData.distanceMatrix.length];
//
//	    for (int i = 0; i < inputData.distanceMatrix.length; i++)
//        {
//            arrivalTimes[i] = Integer.MAX_VALUE;
//            sptSet[i] = false;
//            previousNodes[i] = -1;
//        }
//
//	    arrivalTimes[departureNode] = departureTime;
//
//        for (int count = 0; count < inputData.distanceMatrix.length -1; count++)
//        {
//            int u = minDistance(arrivalTimes, sptSet);
//
//            sptSet[u] = true;
//
//            for (int v = 0; v < inputData.distanceMatrix.length; v++) {
//                if(!sptSet[v] && inputData.distanceMatrix[u][v]!=0) {
//                    double arrivalTime = travelTimeCalculate(inputData.distanceMatrix[u][v],arrivalTimes[u],inputData.speedFunctionList.get(inputData.speedFunctionMatrix[u][v]-1));
//                    if (arrivalTime < arrivalTimes[v]) {
//                        arrivalTimes[v] = arrivalTime;
//                        previousNodes[v] = u;
//                    }
//                }
//            }
//        }
//
//        ArrayList<String> nodePath = new ArrayList<>();
//        nodePath.add(InputData.nodeName[targetIndex]);
//        int index = targetIndex;
//        while(!InputData.nodeName[index].equals(InputData.nodeName[sourceIndex])){
//            float inf = Integer.MAX_VALUE;
//            int tempIndex = 0;
//            for(int i=0;i < InputData.distanceMatrix.length;i++){
//            if(InputData.distanceMatrix[i][index] != 0f && arrivalTimes[i] < inf){
//                inf = arrivalTimes[i];
//                tempIndex = i;
//            }
//            }
//            index = tempIndex;
//            nodePath.add(InputData.nodeName[index]);
//        }
//
//        return nodePath;
//    }
//
//    private static int minDistance(double[] dist, boolean[] sptSet)
//	{
//		double min = Integer.MAX_VALUE;
//		int min_index=-1;
//
//		for (int v = 0; v < dist.length; ++v) {
//            if (!sptSet[v] && dist[v] <= min) {
//                min = dist[v];
//                min_index = v;
//            }
//        }
//
//		return min_index;
//	}
//
//    public static double travelTimeCalculate(double distance, double departureTime, double [][] f){
//        double t=departureTime;
//        double d=0;
//
//        int step = 0;
//        double endOfTheDay = f[f.length - 1][0];
//
//        // Find the step of the speed function for this departure time.
//        while(departureTime % endOfTheDay > f[step][0]) {
//            step = step + 1;
//        }
//
//        while(true){
//            double tt = t % endOfTheDay;
//
//            double distanceTranche = (f[step][0]-tt)*f[step][1];
//
//            if(d+distanceTranche >= distance){
//                t=t+(distance-d)/f[step][1];
//                break;
//            }
//
//            t=f[step][0];
//            d=d+distanceTranche;
//            step = step + 1;
//
//            if(step == f.length){
//                step = 0;
//            }
//        }
//
//        // Felix : Seems to return arrival time
//        return t;
//    }
//
////public static String pathDisplay(ArrayList<String> path){
////    String pathString ="(";
////
////    for(int i = path.size()-1; i > -1;i--)
////        pathString = pathString + path.get(i) + ", ";
////
////   return pathString.substring(0, pathString.length() - 2)+")";
////}
////
////
////
//}
//
