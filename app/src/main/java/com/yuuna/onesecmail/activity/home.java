package com.yuuna.onesecmail.activity;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.yuuna.onesecmail.util.RetrofitClient.retrofitAPI;
import static com.yuuna.onesecmail.util.SharedPreferences.OneSecMail;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_DOMAIN;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_SAVED;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_USERNAME;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuuna.onesecmail.R;
import com.yuuna.onesecmail.model.HomeModel;
import com.yuuna.onesecmail.model.MessageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class home extends Fragment {

    private Spinner spnDomain, spnSaved;
    private EditText edUsername;
    private TextView tvEmail;

    private ClipboardManager myClipboard;
    private ArrayList<HomeModel> homeModelArrayList;
    private SharedPreferences home;

    private String login, domain, svLogin, svDomain, setUser, setDomain, tMerge, sMerge, lCheck, dCheck, sDelete;
    private Boolean bCheck = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEmail = view.findViewById(R.id.hEmail);
        edUsername = view.findViewById(R.id.hUsername);
        spnDomain = view.findViewById(R.id.hDomain);

        home = getActivity().getSharedPreferences(OneSecMail, Context.MODE_PRIVATE);

        loadData();

        view.findViewById(R.id.hCopyEmail).setOnClickListener(v -> {
            myClipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
            myClipboard.setPrimaryClip(ClipData.newPlainText("email", tvEmail.getText().toString()));

            Toast.makeText(getActivity(), "Email Copied", Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.hCreate).setOnClickListener(v -> {
            if (edUsername.getText().toString().isEmpty()){
                edUsername.setText(generateString(10));
                createEmail();
            } else createEmail();
        });
        view.findViewById(R.id.hLoad).setOnClickListener(v -> showSavedEmail());

        return view;
    }

    private void createEmail() {
        login = edUsername.getText().toString();
        domain = spnDomain.getSelectedItem().toString();

        retrofitAPI.getAllMessage("getMessages", login, domain).enqueue(new Callback<ArrayList<MessageModel>>() {
            @Override
            public void onResponse(Call<ArrayList<MessageModel>> call, Response<ArrayList<MessageModel>> response) {
                if (response.isSuccessful()) {
                    tvEmail.setText(login + "@" + domain);
                    sMerge = tvEmail.getText().toString();
                    home.edit().putString(TAG_USERNAME, login).putString(TAG_DOMAIN, domain).apply();

                    saveEmail();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MessageModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void saveEmail() {
        if (homeModelArrayList.size() == 0) {
            homeModelArrayList.add(new HomeModel(login, domain));
            home.edit().putString(TAG_SAVED, new Gson().toJson(homeModelArrayList)).apply();
        } else {
            for (int i = 0; i < homeModelArrayList.size(); i++) {
                if (sMerge.equals(homeModelArrayList.get(i).getUsername() + "@" + homeModelArrayList.get(i).getDomain())) bCheck = true;
            }
            if (!bCheck) {
                homeModelArrayList.add(new HomeModel(login, domain));
                home.edit().putString(TAG_SAVED, new Gson().toJson(homeModelArrayList)).apply();
            } else {
                bCheck = false;
                Toast.makeText(getActivity(), "Your email already saved!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadData() {
        setUser = home.getString(TAG_USERNAME, "");
        setDomain = home.getString(TAG_DOMAIN, "");

        loadDomain();
        if (!setUser.equals("") && !setDomain.equals("")) {
            tvEmail.setText(setUser + "@" + setDomain);
            edUsername.setText(setUser);
        }
        homeModelArrayList = new Gson().fromJson(home.getString(TAG_SAVED, ""), new TypeToken<ArrayList<HomeModel>>() {}.getType());
        if (homeModelArrayList == null) homeModelArrayList = new ArrayList<>();
    }

    private String generateString(int lenght){
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < lenght; i++) stringBuilder.append(chars[new Random().nextInt(chars.length)]);
        return stringBuilder.toString();
    }

    private void loadDomain() {
        retrofitAPI.getDomain("getDomainList").enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    if (getActivity() != null) {
                        spnDomain.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.spinner_item, response.body()));
                        spnDomain.setSelection(getPositionDomain());
                    }
                } else Toast.makeText(getActivity(), "Fail to get domain", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private int getPositionDomain() {
        for (int i = 0; i < spnDomain.getCount(); i++) if (setDomain.equals(spnDomain.getItemAtPosition(i))) return i;
        return 0;
    }

    private void showSavedEmail() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.dialog_load);

        spnSaved = bottomSheetDialog.findViewById(R.id.spn_saved);

        ArrayList<String> savedList = new ArrayList<>();
        for (int i = 0; i < homeModelArrayList.size(); i++) {
            savedList.add(homeModelArrayList.get(i).getUsername()+"@"+homeModelArrayList.get(i).getDomain());
        }
        spnSaved.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.spinner_item, savedList));
        spnSaved.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                svLogin = homeModelArrayList.get(i).getUsername();
                svDomain = homeModelArrayList.get(i).getDomain();
                sDelete = String.valueOf(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        bottomSheetDialog.show();

        bottomSheetDialog.findViewById(R.id.btn_use).setOnClickListener(v -> {
            home.edit().putString(TAG_USERNAME, svLogin).putString(TAG_DOMAIN, svDomain).apply();
            loadData();
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            if (sDelete != null) {
                if (homeModelArrayList.size() != 0) {
                    homeModelArrayList.remove(Integer.parseInt(sDelete));
                    home.edit().putString(TAG_SAVED, new Gson().toJson(homeModelArrayList)).apply();
                    Toast.makeText(getActivity(), svLogin + "@" + svDomain + " was deleted successfully", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            }
        });
    }
}