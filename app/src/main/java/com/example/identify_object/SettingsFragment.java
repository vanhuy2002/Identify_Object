package com.example.identify_object;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.identify_object.authenticate.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private GoogleSignInClient gsc;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences beep_SP;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        AppCompatImageButton btnLogout = view.findViewById(R.id.btn_logout);
        SwitchCompat sw_beep = view.findViewById(R.id.sw_beep);
        beep_SP = requireActivity().getSharedPreferences("sp_beep", Context.MODE_PRIVATE);
        sw_beep.setChecked(beep_SP.getBoolean("sp_beep", false));
        sw_beep.setOnCheckedChangeListener((compoundButton, isChoose) -> {
            SharedPreferences.Editor editor = beep_SP.edit();
            if (isChoose) {
                editor.putBoolean("sp_beep", true);
                editor.apply();
                sw_beep.setChecked(true);
            } else {
                editor.putBoolean("sp_beep", false);
                editor.apply();
                sw_beep.setChecked(false);
            }
        });
        mAuth = FirebaseAuth.getInstance();

        gsc = GoogleSignIn.getClient(requireContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getResources().getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build());

        btnLogout.setOnClickListener(v -> {
            gsc.signOut();
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        return view;
    }
}
