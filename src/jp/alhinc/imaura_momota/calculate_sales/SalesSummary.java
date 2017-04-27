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
		if(args[0]== null || args.length >= 2){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}


		//支店定義ファイル読み込み
		HashMap<String, String> branchmap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String, Long>();

		try{
			File branchFile = new File(args[0], "branch.lst");

			//エラー処理（No.3）
			if(!branchFile.exists()){
				System.out.println("支店定義ファイルが存在しません");
				return;
			}

			FileReader frBranchFile = new FileReader(branchFile);
			BufferedReader brBranchFile = new BufferedReader(frBranchFile);
			String branchCode;
			while((branchCode = brBranchFile.readLine()) != null){
				String[] branch = branchCode.split(",");

				//支店コードエラー（No.5-No.9）
				if(branch.length <= 1 || branch.length >= 3){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}

				branchmap.put(branch[0], branch[1]);
				branchSaleMap.put(branch[0],0L);

				//支店コードエラー（No.10-No.13）
				if (!branch[0].matches("^[0-9]{3}$")|| branch.length != 2){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
			}
			brBranchFile.close();
		} catch(IOException error) {
			System.out.println("支店定義ファイルが存在しません");
			return;
		}


		//商品定義ファイル読み込み
		HashMap<String,String> commodityMap = new HashMap<String, String>();
		HashMap<String,Long> commoditySaleMap = new HashMap<String,Long>();
		try{

			//商品定義ファイルが存在しない場合（No.14）
			File commodityFile = new File(args[0], "commodity.lst");
			if(!commodityFile.exists()){
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			FileReader frCommodityFile = new FileReader(commodityFile);
			BufferedReader brCommodityFile = new BufferedReader(frCommodityFile);
			String commodityCode;
			while((commodityCode = brCommodityFile.readLine()) != null){
				String[] commodity = commodityCode.split(",");

				commodityMap.put(commodity[0],commodity[1]);
				commoditySaleMap.put(commodity[0],0L);

				//商品コードエラー（No.15-No.22）
				if(!commodity[0].matches("^[A-Za-z0-9]{8}$")||commodity.length != 2){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
			}

			brCommodityFile.close();
		} catch(IOException e){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}

		//集計
		//売り上げファイル
		//rcd拡張子の取得
		File salesFile = new File(args[0]);
		//連番チェック（No.24-No.27）
		if(! salesFile.isDirectory()){
			System.out.println("売上げファイルが連番になっていません");
			return;
		}

		File[] saleCode = salesFile.listFiles();
		ArrayList<File> saleList = new ArrayList<File>();
		for(int i = 0; i< saleCode.length; i++){
			if (saleCode[i].getName().matches("^[0-9]{8}.rcd$")){
				saleList.add (saleCode[i]);
				System.out.println(saleList.get(i).getName());
			}
		}

		//欠番の抽出
		String saleListMax;
		String saleListMin;

		saleListMax = saleList.get(0).getName().substring(0,8);
		saleListMin = saleList.get(saleList.size()-1).getName().substring(0,8);

		int Min = Integer.parseInt(saleListMax);
		int Max = Integer.parseInt(saleListMin);


		if((Max - Min +1) != saleList.size()){
			System.out.println("売り上げファイル名が連番になっていません");
			return;
		}

		//ファイル展開
		//読み込み時はディレクトリから指定する



		try{
			for(int i = 0; i < saleList.size(); i++){

				File file = new File(args[0], saleList.get(i).getName());
				FileReader frsaleList = new FileReader(file);
				BufferedReader brsaleList = new BufferedReader(frsaleList);

		//4回readLineを回す
				String branchNumber = brsaleList.readLine();
				String commodityCode = brsaleList.readLine();

				//売上げファイルが2行以下の場合
				if(branchNumber==null || commodityCode ==null ){
					System.out.println("＜" + saleList.get(i).getName() + "＞のフォーマットが不正です");
					brsaleList.close();
					return;
				}

				Long amount =Long.parseLong(brsaleList.readLine());
				String muda = brsaleList.readLine();

		//エラーの処理
				if(!branchmap.containsKey(branchNumber)){
					System.out.println("＜"+ saleList.get(i).getName() + "＞の支店コードが不正です");
					brsaleList.close();
					return;
				}

				if(! commodityMap.containsKey(commodityCode)){
					System.out.println("＜"+ saleList.get(i).getName() + "＞の商品コードが不正です");
					brsaleList.close();
					return;
				}


				//売上げファイルの中身が4行以上ある場合(No.11)
				if(muda != null){
					System.out.println("＜"+ saleList.get(i).getName() + "＞のフォーマットが不正です");
					brsaleList.close();
					return;
				}

				Long branchTotalAmount;
				Long commodityTotalAmount;


				branchTotalAmount = amount + branchSaleMap.get(branchNumber);
				System.out.println("支店別売上集計は" + branchTotalAmount);
				//マップに返す作業
				branchSaleMap.put(branchNumber, branchTotalAmount);

				commodityTotalAmount = amount + commoditySaleMap.get(commodityCode);
				System.out.println("商品別売上集計は" + commodityTotalAmount);
				//マップに返す作業
				commoditySaleMap.put(commodityCode, commodityTotalAmount);

				if(!branchTotalAmount.toString().matches("^\\d{1,10}$")||!commodityTotalAmount.toString().matches("^\\d{1,10}$")){
					System.out.println("合計金額が10桁を超えました");
					brsaleList.close();
					return;
				}
				brsaleList.close();

				System.out.println(branchNumber + "," + branchmap.get(branchNumber) + "," +  branchTotalAmount);
			}
		}catch(IOException e){
			return;
		}

		//集計結果出力

		//ファイル作成
		//支店別集計ファイル
		 try{
			 File branchDetail = new File(args[0], "branch.out");
			 FileWriter fwbranchDetail = new FileWriter(branchDetail);
			 BufferedWriter bwbranchDetail = new BufferedWriter(fwbranchDetail);

		//降順の作成
			 List<Map.Entry<String,Long>> entries =  new ArrayList<>(branchSaleMap.entrySet());
			 Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				 public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
					 return (entry2.getValue()).compareTo(entry1.getValue());
				 }
			 });

		//書き込み
			 for (Entry<String,Long> s : entries) {
				 System.out.println("s.getKey() : " + s.getKey());
				 System.out.println("s.getValue() : " + s.getValue());

				 bwbranchDetail.write(s.getKey() + "," + branchmap.get(s.getKey()) + "," + s.getValue());
				 bwbranchDetail.newLine();
			 }

			 bwbranchDetail.close();
		 } catch(IOException e){
			 System.out.println("予期せぬエラーが発生しました");
			 return;
		 }


		//商品集計ファイル
		 try{
			 File commodityDetail = new File(args[0], "commodity.out");
			 FileWriter fwcommodityDetail = new FileWriter(commodityDetail);
			 BufferedWriter bwcommodityDetail = new BufferedWriter(fwcommodityDetail);

		//降順の作成
			 List<Map.Entry<String,Long>> entries =  new ArrayList<>(commoditySaleMap.entrySet());
			 Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

				 public int compare(Entry<String,Long> entry1, Entry<String,Long> entry2) {
					 return (entry2.getValue()).compareTo(entry1.getValue());
				 }
			 });

		//書き込み
			 for (Entry<String,Long> str : entries) {
				 System.out.println("str.getKey() : " + str.getKey());
				 System.out.println("str.getValue() : " + str.getValue());

				 bwcommodityDetail.write(str.getKey() + "," + commodityMap.get(str.getKey()) + "," + str.getValue());
				 bwcommodityDetail.newLine();
			 }

			 bwcommodityDetail.close();
		 } catch(IOException e){
			 System.out.println("予期せぬエラーが発生しました");
			 return;
		 }




	}


}
