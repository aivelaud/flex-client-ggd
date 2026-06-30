#pragma once
// ──────────────────────────────────────────────────────────────
//  Dobby inline hook wrapper (ARM64)
//  https://github.com/jmpews/Dobby
//
//  Kullanım:
//    DobbyHook((void*)originalFn, (void*)hookFn, (void**)&originalFn);
// ──────────────────────────────────────────────────────────────

extern "C" {
    int DobbyHook(void* address, void* replace, void** origin);
    int DobbyDestroy(void* address);
}
