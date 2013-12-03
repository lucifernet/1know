package tw.com.ischool.oneknow.item;

import org.json.JSONObject;

import tw.com.ischool.oneknow.R;
import tw.com.ischool.oneknow.login.ILoginHelper.OnLoginResultListener;
import tw.com.ischool.oneknow.login.SwitchLoginHelper;
import tw.com.ischool.oneknow.main.MainActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SwitchGuestItem extends ActionItem {
	public SwitchGuestItem() {
		super.init(R.string.item_switch, android.R.drawable.ic_menu_agenda,
				DisplayStatus.GUEST, new PaddingTask() {

					@Override
					public void invoke(final Activity activity) {
						// get prompts.xml view
						LayoutInflater li = LayoutInflater.from(activity);
						View promptsView = li.inflate(
								R.layout.dialog_switch_user, null);

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								activity);

						// set prompts.xml to alertdialog builder
						alertDialogBuilder.setView(promptsView);

						final EditText userInput = (EditText) promptsView
								.findViewById(R.id.editTextDialogUserInput);

						// set dialog message
						alertDialogBuilder
								.setCancelable(false)
								.setPositiveButton(R.string.switch_confirm,
										new DialogInterface.OnClickListener() {
											public void onClick(
													final DialogInterface dialog,
													int id) {

												SwitchLoginHelper helper = new SwitchLoginHelper(
														activity, userInput
																.getText()
																.toString() + "@1know.net");
												helper.setListener(new OnLoginResultListener() {

													@Override
													public void onSucceed(
															JSONObject json,
															DisplayStatus status) {
														MainActivity main = (MainActivity) activity;
														main.switchUser(status,
																json);
														dialog.dismiss();
													}

													@Override
													public void onFail(
															String message,
															Exception e) {
														String error = activity
																.getString(R.string.switch_failure);
														error = String.format(
																error, message);
														Toast.makeText(
																activity,
																error,
																Toast.LENGTH_LONG)
																.show();
													}
												});
												helper.login();
											}
										})
								.setNegativeButton(R.string.switch_cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});

						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();

						// show it
						alertDialog.show();

					}
				});
	}
}
