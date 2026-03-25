package com.outsystems.mlkitscanner;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

public class MLKitScannerPlugin extends CordovaPlugin {

    private static final int START_DOCUMENT_SCAN_REQUEST_CODE = 1001;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("scanDocument".equals(action)) {
            this.callbackContext = callbackContext;
            startScan();
            return true;
        }
        return false;
    }

    private void startScan() {
        // Configurar o Scanner da Google (Limite de 1 página, apenas formato JPEG)
        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(false)
                .setPageLimit(1)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .build();

        GmsDocumentScanner scanner = GmsDocumentScanning.getClient(options);

        scanner.getStartScanIntent(cordova.getActivity())
                .addOnSuccessListener(intentSender -> {
                    try {
                        // Avisar o Cordova que vamos esperar por um resultado de outra Activity
                        cordova.setActivityResultCallback(this);
                        cordova.getActivity().startIntentSenderForResult(intentSender, START_DOCUMENT_SCAN_REQUEST_CODE, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        callbackContext.error("Erro ao abrir a câmara: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    callbackContext.error("Falha ao iniciar o scanner: " + e.getMessage());
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_DOCUMENT_SCAN_REQUEST_CODE) {
            GmsDocumentScanningResult result = GmsDocumentScanningResult.getFromActivityResult(data);
            
            if (resultCode == Activity.RESULT_OK && result != null) {
                // Sucesso! O Scanner devolveu a imagem cortada
                if (!result.getPages().isEmpty()) {
                    Uri imageUri = result.getPages().get(0).getImageUri();
                    callbackContext.success(imageUri.toString());
                } else {
                    callbackContext.error("Nenhuma imagem capturada.");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Utilizador cancelou a operação
                callbackContext.error("Cancelado pelo utilizador.");
            } else {
                callbackContext.error("Erro desconhecido durante o scan.");
            }
        }
    }
}