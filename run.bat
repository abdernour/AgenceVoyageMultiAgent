@echo off
REM Build and Run Script for AgenceVoyageMultiAgent
REM This is a simple batch file wrapper for the PowerShell script

powershell.exe -ExecutionPolicy Bypass -File "%~dp0run.ps1"
pause
