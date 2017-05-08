package jp.alhinc.imaura_momota.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class SalesSummary {
	/**
	 * @param args
	 */
	public static void main (String[]args){

		//コマンドライン引数のエラー処理（No1.No.2)
		if(args.length != 1 ){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//HashMapの作成
		HashMap<String, String> branchmap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String, Long>();
		HashMap<String,String> commodityMap = new HashMap<String, String>();
		HashMap<String,Long> commoditySaleMap = new HashMap<String,Long>();

		//支店定義ファイル読み込み
		if(!branchFileRead(args[0], "branch.lst", branchmap, branchSaleMap)){
			return ;
		}

		//商品定義ファイル読み込み
		if(!commodityFileRead(args[0],"commodity.lst",commodityMap, commoditySaleMap)){
			return ;
		}

		//集計
		//売り上げファイル
		//rcd拡張子の取得
		File salesFile = new File(args[0]);
		//連番チェック（No.24-No.27）

		File[] saleCode = salesFile.listFiles();
		ArrayList<File> saleList = new ArrayList<File>();
		for(int i = 0; i< saleCode.length; i++){
			if(saleCode[i].isFile() && saleCode[i].getName().matches("^[0-9]{8}.rcd$")){
				saleList.add (saleCode[i]);
			}
		}

		//欠番の抽出
		String saleListMax;
		String saleListMin;

		saleListMin = saleList.get(0).getName().substring(0,8);
		saleListMax = saleList.get(saleList.size()-1).getName().substring(0,8);

		int Min = Integer.parseInt(saleListMin);
		int Max = Integer.parseInt(saleListMax);

		if((Max - Min +1) != saleList.size()){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		//ファイル展開
		//読み込み時はディレクトリから指定する
		BufferedReader brsaleList = null;
		try{
			for(int i = 0; i < saleList.size(); i++){

				File file = new File(args[0], saleList.get(i).getName());
				brsaleList = new BufferedReader(new FileReader(file));
				//4回readLineを回す
				String branchNumber = brsaleList.readLine();
				String commodityCode = brsaleList.readLine();

				//売上げファイルが2行以下の場合
				if(branchNumber==null || commodityCode ==null ){
					System.out.println(saleList.get(i).getName() + "のフォーマットが不正です");
					brsaleList.close();
					return;
				}

				String amount = brsaleList.readLine();
				if (!amount.matches("^\\d{1,10}$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				Long amountNum =Long.parseLong(amount);
				String muda = brsaleList.readLine();

				//エラーの処理
				if(!branchmap.containsKey(branchNumber)){
					System.out.println(saleList.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if(! commodityMap.containsKey(commodityCode)){
					System.out.println(saleList.get(i).getName() + "の商品コードが不正です");
					return;
				}


				//売上げファイルの中身が4行以上ある場合(No.11)
				if(muda != null){
					System.out.println(saleList.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				Long branchTotalAmount;
				Long commodityTotalAmount;

				branchTotalAmount = amountNum + branchSaleMap.get(branchNumber);
				//マップに返す作業
				branchSaleMap.put(branchNumber, branchTotalAmount);

				commodityTotalAmount = amountNum + commoditySaleMap.get(commodityCode);
				//マップに返す作業
				commoditySaleMap.put(commodityCode, commodityTotalAmount);

				if(!branchTotalAmount.toString().matches("^\\d{1,10}$")||!commodityTotalAmount.toString().matches("^\\d{1,10}$")){
					System.out.println("合計金額が10桁を超えました");
					brsaleList.close();
					return;
				}
			}

		} catch(IOException e){
			return;

		} finally{
			try{
				if(brsaleList != null){
					brsaleList.close();
				}
			} catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
			}
		}

		//集計結果出力
		//支店別集計ファイル
		if(!FileOut(args[0],"branch.out",branchmap,branchSaleMap)){
			return;
		}

		//商品集計ファイル出力
		if(!FileOut(args[0],"commodity.out",commodityMap,commoditySaleMap)){
			return;
		}
	}

	public static boolean branchFileRead(String dirPath, String fileName,  HashMap<String, String>names, HashMap<String, Long>sales){
		BufferedReader brBranchFile = null;
		try{
			File branchFile = new File(dirPath, fileName);
			//エラー処理（No.3）
			if(!branchFile.exists()){
				System.out.println("支店定義ファイルが存在しません");
				return false;
			}
			brBranchFile = new BufferedReader(new FileReader(branchFile));
			String branchCode;
			while((branchCode = brBranchFile.readLine()) != null){
				String[] branch = branchCode.split(",");

				//支店コードエラー（No.5-No.9）
				if(branch.length <= 1 || branch.length >= 3){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return false;
				}

				names.put(branch[0], branch[1]);
				sales.put(branch[0],0L);

				//支店コードエラー（No.10-No.13）
				if (!branch[0].matches("^[0-9]{3}$")|| branch.length != 2){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return false;
				}
			}

		} catch(IOException error) {
			System.out.println("支店定義ファイルが存在しません");
			return false;

		} finally{
			if(brBranchFile != null){
				try{
					brBranchFile.close();
				} catch(IOException e) {
					System.out.println("予期せぬエラーが発生しました");
				}
			}
		}
		return true;
	}

	public static boolean commodityFileRead(String dirPath, String fileName, HashMap<String, String>names, HashMap<String, Long>sales){
		BufferedReader brCommodity = null;

		try{
			//商品定義ファイルが存在しない場合（No.14）
			File commodityFile = new File(dirPath, fileName);
			if(!commodityFile.exists()){
				System.out.println("商品定義ファイルが存在しません");
				return false;
			}
			brCommodity = new BufferedReader(new FileReader(commodityFile));
			String commodityCode;
			while((commodityCode = brCommodity.readLine()) != null){
				String[] commodity = commodityCode.split(",");


				//商品コードエラー（No.15-No.22）
				if(!commodity[0].matches("^[A-Za-z0-9]{8}$")||commodity.length != 2){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return false;
				}

				names.put(commodity[0],commodity[1]);
				sales.put(commodity[0],0L);

			}

		} catch(IOException e){
			System.out.println("商品定義ファイルが存在しません");
			return false;

		} finally{
			if(brCommodity != null){
				try{
					brCommodity.close();
				} catch(IOException e) {
					System.out.println("予期せぬエラーが発生しました");
				}
			}
		}
		return true;
	}

	public static boolean FileOut(String dirPath, String fileName, HashMap<String, String>names, HashMap<String, Long>sales){
		BufferedWriter bwbranchDetail = null;
		 try{
			 bwbranchDetail = new BufferedWriter(new FileWriter(new File(dirPath, fileName)));

			 //降順の作成
			 List<Map.Entry<String,Long>> entries =  new ArrayList<>(sales.entrySet());
			 Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				 public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
					 return (entry2.getValue()).compareTo(entry1.getValue());
				 }
			 });

			 //書き込み
			 for (Entry<String,Long> s : entries) {
				 bwbranchDetail.write(s.getKey() + "," + names.get(s.getKey()) + "," + s.getValue());
				 bwbranchDetail.newLine();
			 }

		 } catch(IOException e){
			 System.out.println("予期せぬエラーが発生しました");
			 return false;

		 } finally{
			 if(bwbranchDetail != null){
				 try{
					 bwbranchDetail.close();
				 } catch(IOException e) {
					 System.out.println("予期せぬエラーが発生しました");
				 }
			 }
		 }
		 return true;
	}
}