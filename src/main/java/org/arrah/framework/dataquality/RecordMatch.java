package org.arrah.framework.dataquality;

/***************************************************
 *     Copyright to Amish Choudhary    2015        *
 *                                                 *
 * Any part of code or file can be changed,        *
 * redistributed, modified with the copyright      *
 * information intact                              *
 *                                                 *
 * Author$ : Amish Choudhary                       *
 * Author$ : Vivek Singh                           *
 *                                                 *
 **************************************************/

/*
 * This class is wrapper class on SimMetrics util
 * which is used for matching records using fuzziness
 * it will use following class for similarity test
 * import org.simmetrics.metrics.*;
 *
 */

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.arrah.framework.util.StringCaseFormatUtil;

public class RecordMatch 
{
	/*
	 * Returns the results
	 */
	public class Result implements Comparable<Object>
	{
		
		int index1;
		int index2;
		List<String> record1;
		List<String> record2;
		boolean isMatch;
		float simMatchValResult;
		
		Result(boolean isMatch, int index1, int index2, List<String> record1, List<String> record2, float simMatchVal)
		{
			this.isMatch = isMatch;
			this.index1 = index1;
			this.simMatchValResult = simMatchVal;
			if(isMatch)
			{
				
				this.index2 = index2;
				this.record1 = new ArrayList<String>(record1);
				this.record2 = new ArrayList<String>(record2);
			}
		}
		
		public int getLeftMatchIndex() {
			return index1;
		}
		public int getRightMatchIndex() {
			return index2;
		}
		public List<String> getLeftMatchedRow() {
			return record1;
		}
		public List<String> getRightMatchedRow() {
			return record2;
		}
		public boolean isMatch() {
			return isMatch;
		}
		public float getSimMatchVal() {
			return simMatchValResult;
		}
		
		public String toString()
		{
			if(isMatch)
			{
				return "Index [" + index1 + "] matched [" + index2 + "]";
			}
			else
			{
				return "Index [" + index1 + "] no match";
			}
		}

		
		@Override
		public int compareTo(Object o) {
			Result newo = (Result)o;
			if ( this.index1 > newo.index1) 
				 return 1;
			 if ( this.index1 < newo.index1) 
				 return  -1;
			return 0;
		}
		
	}

	public class ColData
	{
		/*
		 * Column name
		 */
		private int m_colIndexA;
		private int m_colIndexB;
		private float m_matchIndex;
		private String m_algoName;
		public ColData(int colIndexA, int colIndexB, float matchIndex, String algoName)
		{
			m_colIndexA = colIndexA;
			m_colIndexB = colIndexB;
			m_matchIndex = matchIndex;
			m_algoName = algoName;
		}
		

		public float getM_matchIndex() {
			return m_matchIndex;
		}
		public void setM_matchIndex(float m_matchIndex) {
			this.m_matchIndex = m_matchIndex;
		}
		public int getM_colIndexA() {
			return m_colIndexA;
		}
		public void setM_colIndexA(int m_colIndexA) {
			this.m_colIndexA = m_colIndexA;
		}
		public int getM_colIndexB() {
			return m_colIndexB;
		}
		public void setM_colIndexB(int m_colIndexB) {
			this.m_colIndexB = m_colIndexB;
		}
		public String getM_algoName() {
			return m_algoName;
		}
		public void setM_algoName(String m_algoName) {
			this.m_algoName = m_algoName;
		}
	};
	
	public class MultiColData
	{
		private List<ColData> colA;
		private List<ColData> colB;
		private String algoName;
		private boolean exactMatch;
		private boolean firstRecordMatch;
	
		public MultiColData()
		{
			colA = new ArrayList<ColData>();
			colB = new ArrayList<ColData>();
			algoName = new String();
			exactMatch = true;
			setFirstRecordMatch(false);
		}
		public List<ColData> getA()
		{
			return colA;
		}
		
		public List<ColData> getB()
		{
			return colB;
		}
		
		public void setA(List<ColData> a)
		{
			colA.addAll(a);
		}
		
		public void setB(List<ColData> b)
		{
			colB.addAll(b);
		}
		public String getAlgoName() {
			return algoName;
		}
		public void setAlgoName(String algoName) {
			this.algoName = algoName;
		}
		public boolean isExactMatch() {
			return exactMatch;
		}
		public void setExactMatch(boolean exactMatch) {
			this.exactMatch = exactMatch;
		}
		public boolean isFirstRecordMatch() {
			return firstRecordMatch;
		}
		public void setFirstRecordMatch(boolean firstRecordMatch) {
			this.firstRecordMatch = firstRecordMatch;
		}
		
	}
	
	static ConcurrentMap <String,String> biclassmap;
	static ConcurrentMap<String, Entry<Method, Object>> functor;

	public class  operator
	{
				
		public operator( )
		{

			biclassmap = new ConcurrentHashMap <String,String>();
			functor = new ConcurrentHashMap <String,Entry<Method,Object>>();
			
			biclassmap.put("org.simmetrics.metrics.BlockDistance","compare");
			biclassmap.put("org.simmetrics.metrics.CosineSimilarity","compare");

			biclassmap.put("org.simmetrics.metrics.DiceSimilarity","compare");
			biclassmap.put("org.simmetrics.metrics.EuclideanDistance","compare");
			biclassmap.put("org.simmetrics.metrics.JaccardSimilarity","compare");
			
			biclassmap.put("org.simmetrics.metrics.Jaro","compare");
			biclassmap.put("org.simmetrics.metrics.JaroWinkler","compare");
			biclassmap.put("org.simmetrics.metrics.Levenshtein","compare");

			biclassmap.put("org.simmetrics.metrics.MatchingCoefficient","compare");
			biclassmap.put("org.arrah.framework.dataquality.SimmetricsUtil$MongeElkan","compare");
			biclassmap.put("org.arrah.framework.dataquality.SimmetricsUtil$Soundex","compare");
			biclassmap.put("org.arrah.framework.dataquality.SimmetricsUtil$qGramDistance","compare");
			biclassmap.put("org.arrah.framework.dataquality.SimmetricsUtil$DoubleMetaPhone","compare");
			biclassmap.put("org.arrah.framework.dataquality.SimmetricsUtil$CustomNames","compare");
			biclassmap.put("org.simmetrics.metrics.SimonWhite","compare");
			biclassmap.put("org.simmetrics.metrics.NeedlemanWunch","compare");
			biclassmap.put("org.simmetrics.metrics.OverlapCoefficient","compare");

			biclassmap.put("org.simmetrics.metrics.SmithWaterman","compare");
			biclassmap.put("org.simmetrics.metrics.SmithWatermanGotoh","compare");


			String methodName, className;
			
			for(Entry<String, String> m : biclassmap.entrySet())
			{
				try
				{
					methodName = m.getValue();
							
					className = m.getKey();
					Class<?> cls = Class.forName(className);
					Object ob = cls.newInstance();
					
					// Create functor
					if (className.equals("org.simmetrics.metrics.CosineSimilarity") ||
							className.equals("org.simmetrics.metrics.DiceSimilarity") ||
							className.equals("org.simmetrics.metrics.JaccardSimilarity") ||
							className.equals("org.simmetrics.metrics.OverlapCoefficient")) {
						functor.put(new String(className.substring(className.lastIndexOf('.') +1, className.length()).toUpperCase()), new AbstractMap.SimpleEntry<Method,Object>(cls.getDeclaredMethod(methodName, new Class[]{Set.class,Set.class}),ob));
					} else if(className.equals("org.simmetrics.metrics.BlockDistance") || 
							className.equals("org.simmetrics.metrics.EuclideanDistance") ||
							className.equals("org.simmetrics.metrics.MatchingCoefficient") ||
							className.equals("org.arrah.framework.dataquality.SimmetricsUtil$MongeElkan") ||
							className.equals("org.simmetrics.metrics.SimonWhite")) {
						functor.put(new String(className.substring(className.lastIndexOf('.') +1, className.length()).toUpperCase()), new AbstractMap.SimpleEntry<Method,Object>(cls.getDeclaredMethod(methodName, new Class[]{List.class,List.class}),ob));
					}
					else //String Type
						functor.put(new String(className.substring(className.lastIndexOf('.') +1, className.length()).toUpperCase()), new AbstractMap.SimpleEntry<Method,Object>(cls.getDeclaredMethod(methodName, new Class[]{String.class,String.class}),ob));
				}
				catch(ClassNotFoundException cfe)
				{
					System.err.println("Class not found "+ cfe.toString());
					System.err.println("Please make sure simmetrics_core-3.0.0.jar in CLASSPATH");
					return;
				}
				catch(NoSuchMethodException nm)
				{
					System.err.println("Method not found "+ nm.toString());
					return;
				}
				catch(Exception exp)
				{
					System.err.println("Exception: "+ exp.toString());
					return;
				}	
			}
			
		}
		
		public List<Result> compare(List<List<String>> left, List <List<String>> right, MultiColData meta1, MultiColData meta2)
		{
			
			//fuzzyCompare fz = new fuzzyCompare(meta1,false);
			//List<Result> matched = Collections.synchronizedList(new ArrayList<Result>());
			List<Result> matched = new ArrayList<Result>();
			boolean firstRecordMatch = meta1.isFirstRecordMatch();
			
			// Make it multi threaded for faster output
			final int THREADCOUNT = 10;
			//final int THREADCOUNT = 1;  // for testing purpose only
			Thread[] tid = new Thread[THREADCOUNT];
			final int rowthread = left.size() / THREADCOUNT;
			
			List<Result>[] matchedThread = new ArrayList[THREADCOUNT]; // for each thread
			
			for (int i = 0; i < THREADCOUNT; i++) {
				final int tindex = i;
				matchedThread[tindex] = new ArrayList<Result>();
				
				tid[tindex] = new Thread(new Runnable() {
					public void run() {
						List<List<String>> leftsub;
						int lIndex = tindex * rowthread; int rIndex = 0; // leftIndex from where thread starts , right Index zero
						boolean atleastOneRecordmatch;
						fuzzyCompare fz = new fuzzyCompare(meta1,false); // to be hold by thread
						
						if (tindex < THREADCOUNT - 1) 
							 leftsub = left.subList(tindex * rowthread, tindex * rowthread + rowthread);
						else
							 leftsub = left.subList(tindex * rowthread, left.size());
						
								try {
									int rowindex=0; // for instrumentation
									int totalcount=leftsub.size();
									
									for(List<String> l : leftsub)
									{
										//System.out.println((++rowindex) % 1000);
										if ((++rowindex) % 500 == 0 ) { // For progress report
											
											System.out.println(rowindex + " of " + totalcount + " Rows Processed for Thread:" +  tindex
											+" at:" + System.currentTimeMillis());
											System.out.println("Length of Object:" + matchedThread[tindex].size());
										}
										atleastOneRecordmatch = false;
										// System.out.println("Record left" + l);
										//System.out.println("Startloop:" + System.currentTimeMillis());
										for(List<String> r : right)
										{
											 // System.out.println("Record right " + r);
											//synchronized(fz) {
											if(fz.compare(l, r) == 0)
											{
												atleastOneRecordmatch = true;
												//matched.add(new Result(true,lIndex,rIndex,l,r,fz.simMatchVal));
												matchedThread[tindex].add(new Result(true,lIndex,rIndex,l,r,fz.simMatchVal));
												
												
												//System.out.println("Left Index:"+lIndex+" Right Index:"+rIndex);
												//System.out.println("Sim:"+fz.simMatchVal+":"+l.toString()+ " " +r.toString());
												// One row matched
												//If we are looking for only first right to match 
												// first left go to next left row
												if(firstRecordMatch)
													break;
											} //} // synchronized
											rIndex++;
										}
										if(!atleastOneRecordmatch)
										{
											// not showing no matched value
											//nomatch.add(new Result(false,lIndex,-1));
										}
										lIndex++;
										rIndex = 0;	
									}
									
								} catch (Exception e) {
									System.out.println(" Thread Comparison Exception:"+e.getMessage());
								}
					}
				});
				tid[i].start();
			}
			for (int i = 0; i < THREADCOUNT; i++) {
				try {
					tid[i].join();
				} catch (Exception e) {
					System.out.println(" Thread Exception:"+e.getMessage());
				}
			}
			for (int i = 0; i < THREADCOUNT; i++) {
				matched.addAll(matchedThread[i]); // Put into big bucket
			}

			matched.sort(null); // natural sort on left index T
			return matched;
		}
	};
	
	
	/*
	 * Comparator for sorting
	 * Not used for time being
	 */
	class recordSorter implements Comparator<List<String>>
	{
		
		private MultiColData meta;
		boolean leftSide;
		
		public recordSorter(MultiColData metaA, boolean leftSide)
		{
			this.meta = metaA;
			this.leftSide = leftSide;
			
		}
		
		@Override
		public int compare(List<String> o1, List<String> o2) {
			
			StringBuilder lA = new StringBuilder();
			StringBuilder lB = new StringBuilder();
			
			for(ColData dd: meta.getA() )
			{
				if(leftSide)
				{
					
					lA = lA.append(o1.get(dd.getM_colIndexA()));
					lB= lB.append(o2.get(dd.getM_colIndexA()));
				}
				else
				{
					lA = lA.append(o1.get(dd.getM_colIndexA()));
					lB= lB.append(o2.get(dd.getM_colIndexA()));
				}
			}
			
			return lA.toString().compareTo(lB.toString());
		}
		
	}
	
	/*
	 * Comparator for comparing rows
	 */
	
	class  fuzzyCompare implements Comparator<List<String>> 
	{
		private MultiColData metaA;
		private float simMatchVal = 0f; // this will hold the last matched value between 0.00f - 1.00f
		
		public fuzzyCompare(MultiColData metaA,boolean bycell)
		{
			this.metaA = metaA;
		}
		
		public float getsimMatchVal() {
			return simMatchVal;
		}

		@Override
		public int compare(List<String> o1, List<String> o2) 
		{
			boolean exactMatch = metaA.isExactMatch();
			boolean atLeastOneMatch = false;
			float matchprob = 1; // default exact match
			simMatchVal = 0f;
			
				try
				{
					Entry<Method,Object> en = null;
					for(ColData dd: metaA.getA() )
					{
						String algoName=dd.getM_algoName();
						
						if (algoName.compareToIgnoreCase("MongeElkan") == 0 ||
							algoName.compareToIgnoreCase("Soundex") == 0 ||
							algoName.compareToIgnoreCase("qGramDistance") == 0 ||
							algoName.compareToIgnoreCase("DoubleMetaPhone") == 0  ||
							algoName.compareToIgnoreCase("CustomNames") == 0 )
							algoName = "SIMMETRICSUTIL$"+algoName;

						en = functor.get(algoName);
						if((matchprob = dd.getM_matchIndex() )!= 1.0)
						{
							Object ob;
							if (algoName.compareToIgnoreCase("CosineSimilarity") == 0 ||
								algoName.compareToIgnoreCase("DiceSimilarity") == 0 ||
								algoName.compareToIgnoreCase("JaccardSimilarity") == 0 ||
								algoName.compareToIgnoreCase("OverlapCoefficient") == 0 ) {
								ob = en.getKey().invoke(en.getValue(), StringCaseFormatUtil.toSetChar(o1.get(dd.getM_colIndexA())),
										StringCaseFormatUtil.toSetChar(o2.get(dd.getM_colIndexB())));
							} else if (algoName.compareToIgnoreCase("BlockDistance") == 0 ||
										algoName.compareToIgnoreCase("EuclideanDistance") == 0 ||
										algoName.compareToIgnoreCase("MatchingCoefficient") == 0 ||
										algoName.compareToIgnoreCase("SimonWhite") == 0 ) {
								ob = en.getKey().invoke(en.getValue(), StringCaseFormatUtil.toArrayListChar(o1.get(dd.getM_colIndexA())),
										StringCaseFormatUtil.toArrayListChar(o2.get(dd.getM_colIndexB())));
							} else if(algoName.compareToIgnoreCase("SimmetricsUtil$MongeElkan") == 0 ) {
								ob = en.getKey().invoke(en.getValue(), StringCaseFormatUtil.toListString(o1.get(dd.getM_colIndexA())),
										StringCaseFormatUtil.toListString(o2.get(dd.getM_colIndexB())));
							} else
								ob = en.getKey().invoke(en.getValue(), o1.get(dd.getM_colIndexA()),o2.get(dd.getM_colIndexB()));
							
//							 	System.out.printf("\n [Col  [%s] [%s] result %f ] " ,   o1.get(dd.getM_colIndexA()),o2.get(dd.getM_colIndexB()),(float)ob);
//							 	System.out.printf(dd.getM_algoName());
//							 	System.out.println("exactMatch:"+exactMatch);
//							 	System.out.println("atLeastOneMatch:"+atLeastOneMatch);
							
							simMatchVal = (Float)ob; // update the matched or unmatched value
							if(simMatchVal < matchprob)
							{
								if(exactMatch) // exact match is AND operation
								{
									return -1;
								}
							}
							else
							{
								atLeastOneMatch = true;
								
//								System.out.printf("\n [Col  [%s] [%s] result %f ] " ,   o1.get(dd.getM_colIndexA()),o2.get(dd.getM_colIndexB()),simMatchVal);
//							 	System.out.printf(dd.getM_algoName());
								if(!exactMatch) // OR Conditon
									break;
							}
						} // All character Match
						else
						{
							if((o1.get(dd.getM_colIndexA()).compareToIgnoreCase(o2.get(dd.getM_colIndexB()))) == 0)
							{
								simMatchVal = 1.00f ; // exact match
				//				System.out.printf("[Col [%s] [%s] matched]" ,   o1.get(dd.getM_colIndexA()),o2.get(dd.getM_colIndexB()));
								atLeastOneMatch = true;
								if(!exactMatch) // OR Condition any column matched
									break;
							}
							else
							{
								simMatchVal = 0.00f ; // no match
				//				System.out.printf("[Col [%s] [%s] did not matched]" ,   o1.get(dd.getM_colIndexA()),o2.get(dd.getM_colIndexB()));
								if(exactMatch)
								{
									return -1;
								}
							}
						}
						en = null;
					}
				} catch (InvocationTargetException x) {
				    x.printStackTrace();
				    return 1;
				} catch (IllegalAccessException x) {
				    x.printStackTrace();
				    return 1;
				}

			
		 if(exactMatch)
		 {					
			return 0;
		 }
		 else
		 {
			 // For exact Match we returned -1  for the first column mismatch
			 // However for Partial match we continued even after mismatch
			 // So check if atleast one matched
			 // Else return 0;
			 if(atLeastOneMatch)
			 {
				 return 0;
			 }
			 else
			 {
				 return -1;
			 }
			 
		 }
			
		}
	} // End of FuzzyCompare class

	
	/*
	 * simple Comparator for comparing two strings
	 */
	
	public class  fuzzyCompareStrings 
	{
		private float simMatchVal = 0f; // this will hold the last matched value between 0.00f - 1.00f
		private String algo ="";
		
		public fuzzyCompareStrings(String algo)
		{
			this.algo = algo;
		}
		
		public float getsimMatchVal() {
			return simMatchVal;
		}
		
		public String getAlgo() {
			return algo;
		}
		public void setAlgo(String algo) {
			this.algo = algo;
		}

		public float compare(String o1, String o2) 
		{
			simMatchVal = 0f;
			
				try
				{
					Entry<Method,Object> en = null;
						String algoName=algo;
						
						if (algoName.compareToIgnoreCase("MongeElkan") == 0 ||
							algoName.compareToIgnoreCase("Soundex") == 0 ||
							algoName.compareToIgnoreCase("qGramDistance") == 0 ||
							algoName.compareToIgnoreCase("DoubleMetaPhone") == 0  ||
							algoName.compareToIgnoreCase("CustomNames") == 0 )
							algoName = "SIMMETRICSUTIL$"+algoName;

						en = functor.get(algoName);
//						System.out.println(en );
//						System.out.println( en.getValue());
//						System.out.println( en.getKey());

						Object ob;
						if (algoName.compareToIgnoreCase("CosineSimilarity") == 0 ||
							algoName.compareToIgnoreCase("DiceSimilarity") == 0 ||
							algoName.compareToIgnoreCase("JaccardSimilarity") == 0 ||
							algoName.compareToIgnoreCase("OverlapCoefficient") == 0 ) {
							ob = en.getKey().invoke(en.getValue(), StringCaseFormatUtil.toSetChar(o1),
									StringCaseFormatUtil.toSetChar(o2));
						} else if (algoName.compareToIgnoreCase("BlockDistance") == 0 ||
									algoName.compareToIgnoreCase("EuclideanDistance") == 0 ||
									algoName.compareToIgnoreCase("MatchingCoefficient") == 0 ||
									algoName.compareToIgnoreCase("SimonWhite") == 0 ) {
							ob = en.getKey().invoke(en.getValue(), StringCaseFormatUtil.toArrayListChar(o1),
									StringCaseFormatUtil.toArrayListChar(o2));
						} else if(algoName.compareToIgnoreCase("SimmetricsUtil$MongeElkan") == 0 ) {
							ob = en.getKey().invoke(en.getValue(), StringCaseFormatUtil.toListString(o1),
									StringCaseFormatUtil.toListString(o2));
						} else
							ob = en.getKey().invoke(en.getValue(), o1,o2);
						
							en = null;
						return simMatchVal = (Float)ob; // update the matched or unmatched value
				} catch (InvocationTargetException x) {
				    x.printStackTrace();
				    return 0f;
				} catch (IllegalAccessException x) {
				    x.printStackTrace();
				    return 0f;
				}
			
		}
	} // End of FuzzyCompare class
	// For Unit testing
	public static void main(String ... args)
	{ /*
		List<List<String>> lRecordList = new ArrayList<List<String>>();
		
		List<List<String>> rRecordList = new ArrayList<List<String>>();
		try(Scanner lFile = new Scanner( new File(args[0])); Scanner rFile = new Scanner(new File(args[1]));)
		{
			while(lFile.hasNextLine())
			{
				lRecordList.add(Arrays.asList(lFile.nextLine().split("\\s+")));
				
			}
			
			while(rFile.hasNextLine())
			{
				rRecordList.add(Arrays.asList(rFile.nextLine().split("\\s+")));
				
			}
			
			RecordMatch diff = new RecordMatch();
			RecordMatch.ColData col1 = diff.new ColData(0,1,(float)0.8, "LEVENSHTEIN" );
			List<RecordMatch.ColData> diffCols = new ArrayList<RecordMatch.ColData>();
			diffCols.add(col1);
			MultiColData m1 = diff.new MultiColData();
			
			m1.setA(diffCols);
			m1.setAlgoName("LEVENSHTEIN");
			RecordMatch.operator doDiff = diff.new operator();
			doDiff.compare(lRecordList, rRecordList, m1, m1);
			
		//uk.ac.shef.wit.simmetrics.similaritymetrics.
			
		}
		catch (Exception excp)
		{
			excp.printStackTrace();
		} */
	}


}

