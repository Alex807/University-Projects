#include <Windows.h>
#include <winreg.h>
#include <stdio.h>
#include <stdlib.h>

// Function to get computer name from Registry
void GetComputerIdentity()
{
    HKEY regHandle;
    DWORD status, type, bufferSize = 256;
    LPBYTE buffer = (LPBYTE)malloc(bufferSize);

    if (!buffer) {
        printf("Memory allocation failled for 'GetComputerIdentity' function!\n");
        return;
    }

    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ActiveComputerName"), 0, KEY_READ, &regHandle) != ERROR_SUCCESS) {
        printf("Could not open registry key.\n");
        free(buffer);
        return;
    }

    status = RegQueryValueEx(regHandle, TEXT("ComputerName"), NULL, &type, buffer, &bufferSize);
    RegCloseKey(regHandle);

    if (status != ERROR_SUCCESS) {
        printf("Failed to retrieve system name.\n");
        free(buffer);
        return;
    }

    wprintf(TEXT("Device Alias: %s\n"), (LPCWSTR)buffer);
    free(buffer);
}

const wchar_t* KeyToString(HKEY root)
{
    return (root == HKEY_CURRENT_USER) ? TEXT("USER_CONFIG") :
        (root == HKEY_LOCAL_MACHINE) ? TEXT("SYSTEM_CONFIG") :
        TEXT("UNKNOWN_CONFIG");
} 

void DecodeRegistryData(DWORD format, DWORD size, const BYTE* raw) {
    switch (format) {
    case REG_SZ:
    case REG_EXPAND_SZ:
        wprintf(TEXT("%s"), (LPCWSTR)raw);
        break;
    case REG_DWORD:
        if (size == sizeof(DWORD))
            wprintf(TEXT("0x%08X (%u)"), *(DWORD*)raw, *(DWORD*)raw);
        break;
    case REG_QWORD:
        if (size == sizeof(DWORD64))
            wprintf(TEXT("0x%016llX (%llu)"), *(DWORD64*)raw, *(DWORD64*)raw);
        break;
    case REG_BINARY:
        for (DWORD i = 0; i < min(size, 16); i++)
            wprintf(TEXT("%02X "), raw[i]);
        if (size > 16)
            wprintf(TEXT("... (%d bytes remain)"), size - 16);
        break;
    case REG_MULTI_SZ:
        for (const wchar_t* str = (const wchar_t*)raw; *str; str += wcslen(str) + 1)
            wprintf(TEXT("%s, "), str);
        break;
    }
}

// Enhanced function to enumerate startup programs from a specific registry path
void EnumerateStartupPrograms(HKEY registryBase, const wchar_t* keyPath) {
    HKEY key;
    DWORD stat = RegOpenKeyEx(registryBase, keyPath, 0, KEY_READ, &key);

    if (stat != ERROR_SUCCESS) {
        wprintf(TEXT("Unable to read %s startup registry path: %s (Error: %lu)\n"), 
                KeyToString(registryBase), keyPath, stat);
        return;
    }

    DWORD totalEntries = 0, maxNameLength = 0, maxDataSize = 0;
    if (RegQueryInfoKey(key, NULL, NULL, NULL, NULL, NULL, NULL, &totalEntries, &maxNameLength, &maxDataSize, NULL, NULL) != ERROR_SUCCESS) {
        wprintf(TEXT("Failed to fetch registry details for %s.\n"), KeyToString(registryBase));
        RegCloseKey(key);
        return;
    }

    maxNameLength++;
    LPWSTR entryLabel = (LPWSTR)malloc(maxNameLength * sizeof(WCHAR));
    LPBYTE entryData = (LPBYTE)malloc(maxDataSize);

    if (!entryLabel || !entryData) {
        printf("Buffer allocation failed.\n");
        free(entryLabel);
        free(entryData);
        RegCloseKey(key);
        return;
    }

    DWORD index = 0;
    while (index < totalEntries)
    {
        DWORD labelLength = maxNameLength, dataSize = maxDataSize, valueType;
        if (RegEnumValue(key, index, entryLabel, &labelLength, NULL, &valueType, entryData, &dataSize) != ERROR_SUCCESS) {
            wprintf(TEXT("Skipping unreadable registry entry at %d.\n"), index);
            index++;
            continue;
        }

        wprintf(TEXT("Startup Entry: %s\n"), entryLabel);
        wprintf(TEXT("Execution Path: "));
        DecodeRegistryData(valueType, dataSize, entryData);
        wprintf(TEXT("\n\n"));

        index++;
    }

    RegCloseKey(key);
    free(entryLabel);
    free(entryData);
}

int main() {
    GetComputerIdentity();
    
    // System-wide startup locations
    wprintf(TEXT("\n=== SYSTEM-WIDE STARTUP APPLICATIONS ===\n\n"));
    
    // Standard Run keys
    wprintf(TEXT("Checking auto-launch programs in SYSTEM_CONFIG Run...\n"));
    EnumerateStartupPrograms(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run"));
    
    // RunOnce keys
    wprintf(TEXT("Checking auto-launch programs in SYSTEM_CONFIG RunOnce...\n"));
    EnumerateStartupPrograms(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\RunOnce"));
    
    // 32-bit applications on 64-bit Windows
    wprintf(TEXT("Checking auto-launch programs in SYSTEM_CONFIG Run (32-bit)...\n"));
    EnumerateStartupPrograms(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Run"));
    
    wprintf(TEXT("Checking auto-launch programs in SYSTEM_CONFIG RunOnce (32-bit)...\n\n"));
    EnumerateStartupPrograms(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\RunOnce"));
    
    // User-specific startup locations
    wprintf(TEXT("\n=== USER-SPECIFIC STARTUP APPLICATIONS ===\n\n"));
    
    // Standard Run keys
    wprintf(TEXT("Checking auto-launch programs in USER_CONFIG Run...\n"));
    EnumerateStartupPrograms(HKEY_CURRENT_USER, TEXT("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run"));
    
    // RunOnce keys
    wprintf(TEXT("Checking auto-launch programs in USER_CONFIG RunOnce...\n"));
    EnumerateStartupPrograms(HKEY_CURRENT_USER, TEXT("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\RunOnce"));
    
    // 32-bit applications on 64-bit Windows
    wprintf(TEXT("Checking auto-launch programs in USER_CONFIG Run (32-bit)...\n\n"));
    EnumerateStartupPrograms(HKEY_CURRENT_USER, TEXT("SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Run"));
    
    wprintf(TEXT("Checking auto-launch programs in USER_CONFIG RunOnce (32-bit)...\n\n"));
    EnumerateStartupPrograms(HKEY_CURRENT_USER, TEXT("SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\RunOnce"));
   
    return 0;
}