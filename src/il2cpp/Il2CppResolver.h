#pragma once
#include <cstdint>
#include <dlfcn.h>
#include <string>

// ──────────────────────────────────────────────────────────────
//  GGD 4.09.00 — libil2cpp.so offset'leri
//  Kaynak: global-metadata.dat strings analizi
// ──────────────────────────────────────────────────────────────

typedef void* Il2CppObject;
typedef void* Il2CppClass;
typedef void* Il2CppDomain;
typedef void* Il2CppAssembly;
typedef void* Il2CppImage;
typedef void* Il2CppMethod;
typedef void* Il2CppString;
typedef void* Il2CppArray;

// IL2CPP API tiplemeler
typedef Il2CppDomain* (*il2cpp_domain_get_t)();
typedef Il2CppAssembly* (*il2cpp_domain_assembly_open_t)(Il2CppDomain*, const char*);
typedef Il2CppImage* (*il2cpp_assembly_get_image_t)(Il2CppAssembly*);
typedef Il2CppClass* (*il2cpp_class_from_name_t)(Il2CppImage*, const char*, const char*);
typedef Il2CppMethod* (*il2cpp_class_get_method_from_name_t)(Il2CppClass*, const char*, int);
typedef void* (*il2cpp_method_get_object_t)(Il2CppMethod*, Il2CppClass*);
typedef Il2CppObject* (*il2cpp_object_new_t)(Il2CppClass*);
typedef Il2CppString* (*il2cpp_string_new_t)(const char*);
typedef Il2CppClass* (*il2cpp_object_get_class_t)(Il2CppObject*);
typedef Il2CppArray* (*il2cpp_array_new_t)(Il2CppClass*, uintptr_t);

struct Il2CppApi {
    il2cpp_domain_get_t                domain_get;
    il2cpp_domain_assembly_open_t      domain_assembly_open;
    il2cpp_assembly_get_image_t        assembly_get_image;
    il2cpp_class_from_name_t           class_from_name;
    il2cpp_class_get_method_from_name_t class_get_method_from_name;
    il2cpp_object_new_t                object_new;
    il2cpp_string_new_t                string_new;
    il2cpp_object_get_class_t          object_get_class;
    il2cpp_array_new_t                 array_new;

    static Il2CppApi& get() {
        static Il2CppApi instance;
        return instance;
    }

    bool init() {
        void* handle = dlopen("libil2cpp.so", RTLD_NOW | RTLD_GLOBAL);
        if (!handle) return false;
        #define LOAD(name) name = (decltype(name))dlsym(handle, "il2cpp_" #name); if(!name) return false;
        LOAD(domain_get)
        LOAD(domain_assembly_open)
        LOAD(assembly_get_image)
        LOAD(class_from_name)
        LOAD(class_get_method_from_name)
        LOAD(object_new)
        LOAD(string_new)
        LOAD(object_get_class)
        LOAD(array_new)
        #undef LOAD
        return true;
    }
};

// Kısa erişim makroları
#define IL2CPP Il2CppApi::get()

inline Il2CppClass* FindClass(const char* namespaze, const char* klass) {
    auto& api = IL2CPP;
    auto domain = api.domain_get();
    // GGD assemblies
    const char* assemblies[] = {
        "Assembly-CSharp", "Assembly-CSharp-firstpass",
        "Managers", "Objects", "Handlers", nullptr
    };
    for (int i = 0; assemblies[i]; i++) {
        auto asm_ = api.domain_assembly_open(domain, assemblies[i]);
        if (!asm_) continue;
        auto img = api.assembly_get_image(asm_);
        if (!img) continue;
        auto cls = api.class_from_name(img, namespaze, klass);
        if (cls) return cls;
    }
    return nullptr;
}

inline void* GetMethodPtr(const char* namespaze, const char* klass, const char* method, int argc = 0) {
    auto cls = FindClass(namespaze, klass);
    if (!cls) return nullptr;
    return IL2CPP.class_get_method_from_name(cls, method, argc);
}
