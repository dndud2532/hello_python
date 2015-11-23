/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014-2015 Perples, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.perples.recosample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

/**
 * RECOBackgroundRangingService is to monitor regions and range regions when the device is inside in the BACKGROUND.
 * 
 * RECOBackgroundMonitoringService�뒗 諛깃렇�씪�슫�뱶�뿉�꽌 monitoring�쓣 �닔�뻾�븯硫�, �듅�젙 region �궡遺�濡� 吏꾩엯�븳 寃쎌슦 諛깃렇�씪�슫�뱶 �긽�깭�뿉�꽌 ranging�쓣 �닔�뻾�빀�땲�떎.
 */
public class RECOBackgroundRangingService extends Service implements RECOMonitoringListener, RECORangingListener, RECOServiceConnectListener {
	
	/**
	 * We recommend 1 second for scanning, 10 seconds interval between scanning, and 60 seconds for region expiration time. 
	 * 1珥� �뒪罹�, 10珥� 媛꾧꺽�쑝濡� �뒪罹�, 60珥덉쓽 region expiration time�� �떦�궗 沅뚯옣�궗�빆�엯�땲�떎.
	 */
	private long mScanDuration = 1*1000L;
	private long mSleepDuration = 10*1000L;
	private long mRegionExpirationTime = 60*1000L;
	private int mNotificationID = 9999;
	
	private RECOBeaconManager mRecoManager;
	private ArrayList<RECOBeaconRegion> mRegions;

	@Override
	public void onCreate() {
		Log.i("RECOBackgroundRangingService", "onCreate()");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("RECOBackgroundRangingService", "onStartCommand");
		/**
		 * Create an instance of RECOBeaconManager (to set scanning target and ranging timeout in the background.)
		 * If you want to scan only RECO, and do not set ranging timeout in the backgournd, create an instance: 
		 * 		mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), true, false);
		 * WARNING: False enableRangingTimeout will affect the battery consumption.
		 * 
		 * RECOBeaconManager �씤�뒪�꽩�뒪�� �깮�꽦�빀�땲�떎. (�뒪罹� ���긽 諛� 諛깃렇�씪�슫�뱶 ranging timeout �꽕�젙)
		 * RECO留뚯쓣 �뒪罹뷀븯怨�, 諛깃렇�씪�슫�뱶 ranging timeout�쓣 �꽕�젙�븯怨� �떢吏� �븡�쑝�떆�떎硫�, �떎�쓬怨� 媛숈씠 �깮�꽦�븯�떆湲� 諛붾엻�땲�떎.
		 * 		mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), true, false); 
		 * 二쇱쓽: enableRangingTimeout�쓣 false濡� �꽕�젙 �떆, 諛고꽣由� �냼紐⑤웾�씠 利앷��빀�땲�떎.
		 */
		mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), MainActivity.SCAN_RECO_ONLY, MainActivity.ENABLE_BACKGROUND_RANGING_TIMEOUT);
		this.bindRECOService();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i("RECOBackgroundRangingService", "onDestroy()");
		this.tearDown();
		super.onDestroy();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.i("RECOBackgroundRangingService", "onTaskRemoved()");
		super.onTaskRemoved(rootIntent);
	}

	private void bindRECOService() {
		Log.i("RECOBackgroundRangingService", "bindRECOService()");
		
		mRegions = new ArrayList<RECOBeaconRegion>();
		this.generateBeaconRegion();
		
		mRecoManager.setMonitoringListener(this);
		mRecoManager.setRangingListener(this);
		mRecoManager.bind(this);
	}
	
	private void generateBeaconRegion() {
		Log.i("RECOBackgroundRangingService", "generateBeaconRegion()");
		
		RECOBeaconRegion recoRegion;
		
		recoRegion = new RECOBeaconRegion(MainActivity.RECO_UUID, "RECO Sample Region");
		recoRegion.setRegionExpirationTimeMillis(this.mRegionExpirationTime);
		mRegions.add(recoRegion);
	}
	
	private void startMonitoring() {
		Log.i("RECOBackgroundRangingService", "startMonitoring()");
		
		mRecoManager.setScanPeriod(this.mScanDuration);
		mRecoManager.setSleepPeriod(this.mSleepDuration);
		
		for(RECOBeaconRegion region : mRegions) {
			try {
				mRecoManager.startMonitoringForRegion(region);
			} catch (RemoteException e) {
				Log.e("RECOBackgroundRangingService", "RemoteException has occured while executing RECOManager.startMonitoringForRegion()");
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.e("RECOBackgroundRangingService", "NullPointerException has occured while executing RECOManager.startMonitoringForRegion()");
				e.printStackTrace();
			}
		}
	}
	
	private void stopMonitoring() {
		Log.i("RECOBackgroundRangingService", "stopMonitoring()");
		
		for(RECOBeaconRegion region : mRegions) {
			try {
				mRecoManager.stopMonitoringForRegion(region);
			} catch (RemoteException e) {
				Log.e("RECOBackgroundRangingService", "RemoteException has occured while executing RECOManager.stopMonitoringForRegion()");
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.e("RECOBackgroundRangingService", "NullPointerException has occured while executing RECOManager.stopMonitoringForRegion()");
				e.printStackTrace();
			}
		}
	}
	
	private void startRangingWithRegion(RECOBeaconRegion region) {
		Log.i("RECOBackgroundRangingService", "startRangingWithRegion()");
		
		/**
		 * There is a known android bug that some android devices scan BLE devices only once. (link: http://code.google.com/p/android/issues/detail?id=65863)
		 * To resolve the bug in our SDK, you can use setDiscontinuousScan() method of the RECOBeaconManager. 
		 * This method is to set whether the device scans BLE devices continuously or discontinuously. 
		 * The default is set as FALSE. Please set TRUE only for specific devices.
		 * 
		 * mRecoManager.setDiscontinuousScan(true);
		 */
		
		try {
			mRecoManager.startRangingBeaconsInRegion(region);
		} catch (RemoteException e) {
			Log.e("RECOBackgroundRangingService", "RemoteException has occured while executing RECOManager.startRangingBeaconsInRegion()");
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e("RECOBackgroundRangingService", "NullPointerException has occured while executing RECOManager.startRangingBeaconsInRegion()");
			e.printStackTrace();
		}
	}
	
	private void stopRangingWithRegion(RECOBeaconRegion region) {
		Log.i("RECOBackgroundRangingService", "stopRangingWithRegion()");
		
		try {
			mRecoManager.stopRangingBeaconsInRegion(region);
		} catch (RemoteException e) {
			Log.e("RECOBackgroundRangingService", "RemoteException has occured while executing RECOManager.stopRangingBeaconsInRegion()");
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e("RECOBackgroundRangingService", "NullPointerException has occured while executing RECOManager.stopRangingBeaconsInRegion()");
			e.printStackTrace();
		}
	}
	
	private void tearDown() {
		Log.i("RECOBackgroundRangingService", "tearDown()");
		this.stopMonitoring();
		
		try {
			mRecoManager.unbind();
		} catch (RemoteException e) {
			Log.e("RECOBackgroundRangingService", "RemoteException has occured while executing unbind()");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onServiceConnect() {
		Log.i("RECOBackgroundRangingService", "onServiceConnect()");
		this.startMonitoring();
		//Write the code when RECOBeaconManager is bound to RECOBeaconService
	}

	@Override
	public void didDetermineStateForRegion(RECOBeaconRegionState state, RECOBeaconRegion region) {
		Log.i("RECOBackgroundRangingService", "didDetermineStateForRegion()");
		//Write the code when the state of the monitored region is changed
	}

	@Override
	public void didEnterRegion(RECOBeaconRegion region, Collection<RECOBeacon> beacons) {
		/**
		 * For the first run, this callback method will not be called. 
		 * Please check the state of the region using didDetermineStateForRegion() callback method.
		 * 
		 * 理쒖큹 �떎�뻾�떆, �씠 肄쒕갚 硫붿냼�뱶�뒗 �샇異쒕릺吏� �븡�뒿�땲�떎. 
		 * didDetermineStateForRegion() 肄쒕갚 硫붿냼�뱶瑜� �넻�빐 region �긽�깭瑜� �솗�씤�븷 �닔 �엳�뒿�땲�떎.
		 */
		
		//Get the region and found beacon list in the entered region
		Log.i("RECOBackgroundRangingService", "didEnterRegion() - " + region.getUniqueIdentifier());
		this.popupNotification("Inside of " + region.getUniqueIdentifier());
		//Write the code when the device is enter the region
		
		this.startRangingWithRegion(region); //start ranging to get beacons inside of the region
		//from now, stop ranging after 10 seconds if the device is not exited
	}

	@Override
	public void didExitRegion(RECOBeaconRegion region) {
		/**
		 * For the first run, this callback method will not be called. 
		 * Please check the state of the region using didDetermineStateForRegion() callback method.
		 * 
		 * 理쒖큹 �떎�뻾�떆, �씠 肄쒕갚 硫붿냼�뱶�뒗 �샇異쒕릺吏� �븡�뒿�땲�떎. 
		 * didDetermineStateForRegion() 肄쒕갚 硫붿냼�뱶瑜� �넻�빐 region �긽�깭瑜� �솗�씤�븷 �닔 �엳�뒿�땲�떎.
		 */
		
		Log.i("RECOBackgroundRangingService", "didExitRegion() - " + region.getUniqueIdentifier());
		this.popupNotification("Outside of " + region.getUniqueIdentifier());
		//Write the code when the device is exit the region
		
		this.stopRangingWithRegion(region); //stop ranging because the device is outside of the region from now
	}

	@Override
	public void didStartMonitoringForRegion(RECOBeaconRegion region) {
		Log.i("RECOBackgroundRangingService", "didStartMonitoringForRegion() - " + region.getUniqueIdentifier());
		//Write the code when starting monitoring the region is started successfully
	}

	@Override
	public void didRangeBeaconsInRegion(Collection<RECOBeacon> beacons, RECOBeaconRegion region) {
		Log.i("RECOBackgroundRangingService", "didRangeBeaconsInRegion() - " + region.getUniqueIdentifier() + " with " + beacons.size() + " beacons");
		//Write the code when the beacons inside of the region is received
	}
	
	private void popupNotification(String msg) {
		Log.i("RECOBackgroundRangingService", "popupNotification()");
		String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(new Date());
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.logo1)
																				.setContentTitle(msg + " " + currentTime)
																				.setContentText(msg);

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		builder.setStyle(inboxStyle);
		nm.notify(mNotificationID, builder.build());
		mNotificationID = (mNotificationID - 1) % 1000 + 9000;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		//This method is not used
		return null;
	}
	
	@Override
	public void onServiceFail(RECOErrorCode errorCode) {
		//Write the code when the RECOBeaconService is failed.
		//See the RECOErrorCode in the documents.
		return;
	}
	
	@Override
	public void monitoringDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
		//Write the code when the RECOBeaconService is failed to monitor the region.
		//See the RECOErrorCode in the documents.
		return;
	}
	
	@Override
	public void rangingBeaconsDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
		//Write the code when the RECOBeaconService is failed to range beacons in the region.
		//See the RECOErrorCode in the documents.
		return;
	}
}
