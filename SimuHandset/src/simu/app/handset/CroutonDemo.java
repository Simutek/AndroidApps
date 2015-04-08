/*
 * Copyright 2012 - 2013 Benjamin Weiss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simu.app.handset;

import simu.app.handset.R;
import simu.database.AssetsDatabaseManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class CroutonDemo extends SherlockFragmentActivity {

	ViewPager croutonPager;
	MenuItem mt1, mt2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getSupportMenuInflater().inflate(R.menu.main_activity, menu);
		mt1 = menu.findItem(R.id.main_sub_menu_rfid);
		mt2 = menu.findItem(R.id.main_sub_menu_scan);

		final MenuItem plus = (MenuItem) menu.findItem(R.id.main_add);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mt1) {
			Toast.makeText(CroutonDemo.this,
					"the rfid item has been clicked !!", Toast.LENGTH_SHORT)
					.show();
			Intent intent = new Intent(this, VerifyRFID.class);
			finish();
			startActivity(intent);
		} else if (item == mt2) {
			Toast.makeText(CroutonDemo.this,
					"the scan item has been clicked !!", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(CroutonDemo.this,
					"the add item has been  clicked !!", Toast.LENGTH_SHORT)
					.show();
		}
		return super.onOptionsItemSelected(item);
	}

	
	
	enum PageInfo {

		Crouton(R.string.crouton), Exstorage(R.string.exstorage), Search(
				R.string.search), Setting(R.string.setting);
		// Verify(R.string.verify),
		// Upload(R.string.upload),
		// About(R.string.about);

		int titleResId;

		PageInfo(int titleResId) {
			this.titleResId = titleResId;
		}                        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		croutonPager = (ViewPager) findViewById(R.id.crouton_pager);
		croutonPager.setAdapter(new CroutonPagerAdapter(
				getSupportFragmentManager()));
		// ((TitlePageIndicator)
		// findViewById(R.id.titles)).setViewPager(croutonPager);
		((TabPageIndicator) findViewById(R.id.titles))
				.setViewPager(croutonPager);

		AssetsDatabaseManager.initManager(getApplication());
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		ActionBar ab = getSupportActionBar();
		Log.d(null, new String().valueOf(ab.getNavigationMode()));
		// ab.hide();
	}

	@Override
	protected void onDestroy() {
		// Workaround until there's a way to detach the Activity from Crouton
		// while
		// there are still some in the Queue.
		Crouton.clearCroutonsForActivity(this);
		AssetsDatabaseManager.closeAllDatabase();
		super.onDestroy();
	}

	class CroutonPagerAdapter extends FragmentPagerAdapter {

		public CroutonPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			if (PageInfo.Crouton.ordinal() == position) {
				return new CroutonFragment();
			} else if (PageInfo.Exstorage.ordinal() == position) {
				return new ExstorageFragment();
			} else if (PageInfo.Search.ordinal() == position) {
				return new SearchFragment();
			} else if (PageInfo.Setting.ordinal() == position) {
				return new SettingFragment();
			}
			// else if (PageInfo.Verify.ordinal() == position) {
			// return new VerifyFragment();
			// }else if (PageInfo.Upload.ordinal() == position) {
			// return new UploadFragment();
			// }else if (PageInfo.About.ordinal() == position) {
			// return new AboutFragment();
			// }
			return null;
		}

		@Override
		public int getCount() {
			return PageInfo.values().length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return CroutonDemo.this
					.getString(PageInfo.values()[position].titleResId);
		}
	}
}
