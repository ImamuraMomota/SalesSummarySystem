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
	public static void main(String[] args) {

		// コマンドライン引数のエラー処理（No1.No.2)
		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		// HashMapの作成
		HashMap<String, String> branchMap = new HashMap<>();
		HashMap<String, Long> branchSaleMap = new HashMap<>();
		HashMap<String, String> commodityMap = new HashMap<>();
		HashMap<String, Long> commoditySaleMap = new HashMap<>();

		// 支店定義ファイル読み込み
		if (!branchCommodityFileRead(args[0], "branch.lst", "支店", "^[0-9]{3}$", branchMap, branchSaleMap)) {
			return;
		}

		// 商品定義ファイル読み込み
		if (!branchCommodityFileRead(args[0], "commodity.lst", "商品", "^[A-Za-z0-9]{8}$", commodityMap,
				commoditySaleMap)) {
			return;
		}

		// 集計
		// 売り上げファイル
		// rcd拡張子の取得・連番チェック（No.24-No.27）
		File salesFile = new File(args[0]);
		File[] saleCode = salesFile.listFiles();
		ArrayList<File> saleList = new ArrayList<>();
		for (int i = 0; i < saleCode.length; i++) {
			if (saleCode[i].isFile() && saleCode[i].getName().matches("^[0-9]{8}.rcd$")) {
				saleList.add(saleCode[i]);
			}
		}
		Collections.sort(saleList);

		// 欠番の抽出
		String saleListMax = saleList.get(saleList.size() - 1).getName().substring(0, 8);
		String saleListMin = saleList.get(0).getName().substring(0, 8);

		int Min = Integer.parseInt(saleListMin);
		int Max = Integer.parseInt(saleListMax);

		if ((Max - Min + 1) != saleList.size()) {
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		// ファイル展開
		// 読み込み時はディレクトリから指定する
		BufferedReader brsaleList = null;
		try {
			for (int i = 0; i < saleList.size(); i++) {

				brsaleList = new BufferedReader(new FileReader(new File(args[0], saleList.get(i).getName())));
				// 4回readLineを回す
				String branchCode = brsaleList.readLine();
				String commodityCode = brsaleList.readLine();
				String amount = brsaleList.readLine();
				String somethingExceptinoal = brsaleList.readLine();

				// 売上げファイルが2行以下の場合
				if (branchCode == null || commodityCode == null || amount == null || "".equals(amount)) {
					System.out.println(saleList.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				if (!amount.matches("^\\d{1,10}$")) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				Long amountNum = Long.parseLong(amount);

				// エラーの処理
				if (!branchMap.containsKey(branchCode)) {
					System.out.println(saleList.get(i).getName() + "の支店コードが不正です");
					return;
				}

				if (!commodityMap.containsKey(commodityCode)) {
					System.out.println(saleList.get(i).getName() + "の商品コードが不正です");
					return;
				}

				// 売上げファイルの中身が4行以上ある場合(No.11)
				if (somethingExceptinoal != null) {
					System.out.println(saleList.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				Long branchTotalAmount = amountNum + branchSaleMap.get(branchCode);
				Long commodityTotalAmount = amountNum + commoditySaleMap.get(commodityCode);

				if (branchTotalAmount.toString().length() > 10 || commodityTotalAmount.toString().length() > 10) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				// Mapに返す
				branchSaleMap.put(branchCode, branchTotalAmount);
				commoditySaleMap.put(commodityCode, commodityTotalAmount);
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;

		} finally {
			try {
				if (brsaleList != null) {
					brsaleList.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		// 集計結果出力
		// 支店別集計ファイル
		if (!fileOut(args[0], "branch.out", branchMap, branchSaleMap)) {
			return;
		}

		// 商品集計ファイル出力
		if (!fileOut(args[0], "commodity.out", commodityMap, commoditySaleMap)) {
			return;
		}
	}

	public static boolean branchCommodityFileRead(String dirPath, String fileName, String errorWord, String code,
			HashMap<String, String> names, HashMap<String, Long> sales) {
		BufferedReader brBranchCommodityFile = null;
		try {
			File branchCommodityFile = new File(dirPath, fileName);
			// エラー処理（No.3,No.14）
			if (!branchCommodityFile.exists()) {
				System.out.println(errorWord + "定義ファイルが存在しません");
				return false;
			}
			brBranchCommodityFile = new BufferedReader(new FileReader(branchCommodityFile));
			String branchCommodityCode;
			while ((branchCommodityCode = brBranchCommodityFile.readLine()) != null) {
				String[] branchCommodity = branchCommodityCode.split(",");

				// 支店・商品コードエラー（No.4-No.22）
				if (branchCommodity.length != 2 || !branchCommodity[0].matches(code)) {
					System.out.println(errorWord + "定義ファイルのフォーマットが不正です");
					return false;
				}

				names.put(branchCommodity[0], branchCommodity[1]);
				sales.put(branchCommodity[0], 0L);
			}

		} catch (IOException error) {
			System.out.println("支店定義ファイルが存在しません");
			return false;

		} finally {
			if (brBranchCommodityFile != null) {
				try {
					brBranchCommodityFile.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}

	public static boolean fileOut(String dirPath, String fileName, HashMap<String, String> names,
			HashMap<String, Long> sales) {
		BufferedWriter bwbranchDetail = null;
		try {
			bwbranchDetail = new BufferedWriter(new FileWriter(new File(dirPath, fileName)));

			// 降順の作成
			List<Map.Entry<String, Long>> entries = new ArrayList<>(sales.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {

				public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
					return (entry2.getValue()).compareTo(entry1.getValue());
				}
			});

			// 書き込み
			for (Entry<String, Long> s : entries) {
				bwbranchDetail.write(s.getKey() + "," + names.get(s.getKey()) + "," + s.getValue());
				bwbranchDetail.newLine();
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;

		} finally {
			if (bwbranchDetail != null) {
				try {
					bwbranchDetail.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}
}