#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include "malware_scan_JNIHeuristics.h"
#include "libdasm.h"

unsigned char * read_file(int *len, const char *name) {
    char   *buf;
    FILE    *fp;
    struct stat     sstat;

    fp = fopen(name, "r+b");
    stat(name, &sstat);
    *len = sstat.st_size;
    buf = (char *)malloc(*len);

    fread(buf, 1, *len, fp);

    fclose(fp);

    return (buf); 
}

JNIEXPORT jboolean JNICALL Java_malware_scan_JNIHeuristics_heuristicsAnalyze(JNIEnv *env, jobject object,jstring path){
	 INSTRUCTION inst;
	unsigned char *fileData;
	int buffer = 0;
	enum Format format = FORMAT_INTEL; 
	int size; 
	int length;
	char string[256];
	const char *nativeString = (*env)->GetStringUTFChars(env, path, 0); 
    char instructions[1000][256];
	int result = 0;
	
	fileData = read_file(&size, nativeString);
 
	int i = 0;
	while (buffer < size) {
		length = get_instruction(&inst, fileData + buffer, MODE_32);
		
		if (!length || (length + buffer > size)) {
			buffer++;
			continue;
		}
		
		get_instruction_string(&inst, format, (DWORD)buffer, string, sizeof(string));
		strcpy(instructions[i], string); 
		buffer += length; 
		i++;
		if(buffer > 150)
			break;
	} 

	
	int j;
	int rule1 = 2;
	int rule2 = 8;
	
	int suspiciousInst1 = 0;
	int suspiciousInst2 = 0;

	
	for(j=0;j<i;j++)
	{
		if(strstr(instructions[j], "del") != NULL)
		{
		  suspiciousInst1++;
		}
		if(strstr(instructions[j], "mov") != NULL)
		{
		  suspiciousInst2++;
		}
		
	}
	
	if(suspiciousInst1 >= rule1) //|| suspiciousInst2 >= rule2)
	{
		result = 1;
	}
	else
	{
		result = 0;
	}

	(*env)->ReleaseStringUTFChars(env,path,nativeString);  
		
    return 0;
}

