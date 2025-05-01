#include <windows.h>
#include <stdio.h>
#include <winsvc.h>

#define SVCNAME TEXT("RandomService")

SERVICE_STATUS          gSvcStatus;
SERVICE_STATUS_HANDLE   gSvcStatusHandle;
HANDLE                  ghSvcStopEvent = NULL;


VOID SvcInit(DWORD dwArgc, LPTSTR* lpszArgv){
    ghSvcStopEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

    if (ghSvcStopEvent == NULL)
    {
        ReportSvcStatus(SERVICE_STOPPED, GetLastError(), 0);
        return;
    }

    ReportSvcStatus(SERVICE_RUNNING, NO_ERROR, 0);

    while (1){
        WaitForSingleObject(ghSvcStopEvent, INFINITE);
        ReportSvcStatus(SERVICE_STOPPED, NO_ERROR, 0);
        return;
    }
}

//VOID SvcInstall(){
//    SC_HANDLE schSCManager;
//    SC_HANDLE schService;
//    TCHAR szUnquotedPath[MAX_PATH];
//
//    if (!GetModuleFileName(NULL, szUnquotedPath, MAX_PATH)){
//        printf("Cannot install service (%d)\n", GetLastError());
//        return;
//    }
//
//    TCHAR szPath[MAX_PATH];
//    StringCbPrintf(szPath, MAX_PATH, TEXT("\"%s\""), szUnquotedPath);
//    schSCManager = OpenSCManager(NULL,NULL,SC_MANAGER_ALL_ACCESS);
//
//    if (schSCManager == NULL){
//        printf("OpenSCManager failed (%d)\n", GetLastError());
//        return;
//    }
//
//    schService = CreateService(schSCManager,SVCNAME,SVCNAME,SERVICE_ALL_ACCESS,SERVICE_WIN32_OWN_PROCESS,SERVICE_DEMAND_START,SERVICE_ERROR_NORMAL,szPath, NULL, NULL,NULL, NULL, NULL);
//
//    if (schService == NULL){
//        printf("CreateService failed (%d)\n", GetLastError());
//        CloseServiceHandle(schSCManager);
//        return;
//    }
//    else printf("Service installed successfully\n");
//
//    CloseServiceHandle(schService);
//    CloseServiceHandle(schSCManager);
//}

VOID WINAPI SvcCtrlHandler(DWORD dwCtrl)
{
    switch (dwCtrl){
    case SERVICE_CONTROL_STOP:
        ReportSvcStatus(SERVICE_STOP_PENDING, NO_ERROR, 0);

        SetEvent(ghSvcStopEvent);
        ReportSvcStatus(gSvcStatus.dwCurrentState, NO_ERROR, 0);

        return;

    case SERVICE_CONTROL_INTERROGATE:
        break;

    default:
        break;
    }

}

VOID WINAPI SvcMain(DWORD dwArgc, LPTSTR* lpszArgv) {
    gSvcStatusHandle = RegisterServiceCtrlHandler(SVCNAME, SvcCtrlHandler);

    if (!gSvcStatusHandle) {
        SvcReportEvent(TEXT("RegisterServiceCtrlHandler"));
        return;
    }

    gSvcStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    gSvcStatus.dwServiceSpecificExitCode = 0;

    ReportSvcStatus(SERVICE_START_PENDING, NO_ERROR, 3000);

    SvcInit(dwArgc, lpszArgv);
}

int _cdecl _tmain(int argc, TCHAR* argv[]){

	if (lstrcmpi(argv[1], TEXT("install")) == 0) {
		SvcInstall();
		return;
	}

    SERVICE_TABLE_ENTRY DispatchTable[] = { {SVCNAME, (LPSERVICE_MAIN_FUNCTION)SvcMain},{NULL, NULL} };

		//if (!StartServiceCtrlDispatcher(DispatchTable)) {
		//	SvcReportEvent(TEXT("StartServiceCtrlDispatcher"));
		//}
}