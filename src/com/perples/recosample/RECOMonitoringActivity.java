
package com.perples.recosample;

import java.util.ArrayList;
import java.util.Collection;


import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;


import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;


public class RECOMonitoringActivity extends RECOActivity implements RECORangingListener{


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.foodlist_image);
		
	
		mRecoManager.setRangingListener(this);

		mRecoManager.bind(this);
	}
	

	
	@Override
	protected void onDestroy() {
		super.onDestroy();	
		this.stop(mRegions);
		this.unbind();
	}
	
	private void unbind() {
		try {
			mRecoManager.unbind();
		} catch (RemoteException e) {
			Log.i("RECORangingActivity", "Remote Exception");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onServiceConnect() {
		Log.i("RECORangingActivity", "onServiceConnect()");
		mRecoManager.setDiscontinuousScan(MainActivity.DISCONTINUOUS_SCAN);
		this.start(mRegions);
		
	}

	@Override
	public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
		
		
		for(RECOBeacon beacon2 :recoBeacons ){
			int BeaconMajor2 = beacon2.getMajor();
			int BeaconRes2 = beacon2.getRssi();
			
			if(BeaconMajor2 == 17988 && BeaconRes2 < 0.3){
				setContentView(R.layout.gesipan_mainpage);
			
			}
	
		}
	}
	

	
	@Override
	protected void start(ArrayList<RECOBeaconRegion> regions) {
		
	
		for(RECOBeaconRegion region : regions) {
			try {
				mRecoManager.startRangingBeaconsInRegion(region);
			} catch (RemoteException e) {
				Log.i("RECORangingActivity", "Remote Exception");
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.i("RECORangingActivity", "Null Pointer Exception");
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void stop(ArrayList<RECOBeaconRegion> regions) {
		for(RECOBeaconRegion region : regions) {
			try {
				mRecoManager.stopRangingBeaconsInRegion(region);
			} catch (RemoteException e) {
				Log.i("RECORangingActivity", "Remote Exception");
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.i("RECORangingActivity", "Null Pointer Exception");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onServiceFail(RECOErrorCode errorCode) {
		//Write the code when the RECOBeaconService is failed.
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
