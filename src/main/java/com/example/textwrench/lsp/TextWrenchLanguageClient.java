package com.example.textwrench.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.concurrent.CompletableFuture;

public class TextWrenchLanguageClient implements LanguageClient {
    private LanguageServer server;

    public void connect(LanguageServer server) {
        this.server = server;
    }

    @Override
    public void telemetryEvent(Object object) {
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        // Handle diagnostics (e.g., syntax errors)
    }

    @Override
    public void showMessage(MessageParams messageParams) {
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
        return null;
    }

    @Override
    public void logMessage(MessageParams message) {
    }

    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return server.initialize(params);
    }

    public CompletableFuture<Object> shutdown() {
        return server.shutdown();
    }

    public void exit() {
        server.exit();
    }
}