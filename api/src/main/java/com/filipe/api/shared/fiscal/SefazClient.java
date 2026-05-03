package com.filipe.api.shared.fiscal;

public interface SefazClient {
    NfceResponse emitirNfce(NfcePayload payload, String token, String ambiente);
    void cancelarNfce(String chaveAcesso, String motivo, String token, String ambiente);
}
