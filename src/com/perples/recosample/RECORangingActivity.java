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

import java.util.ArrayList;
import java.util.Collection;


import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;


import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;


public class RECORangingActivity extends RECOActivity implements RECORangingListener{


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_ranging);
		
	
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
		
		
		for(RECOBeacon beacon :recoBeacons ){
			int BeaconMajor = beacon.getMajor();
			int BeaconRes = beacon.getRssi();
			
			if(BeaconMajor == 17988 && BeaconRes < 0.3){
				setContentView(R.layout.gesipan_image);
			
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
