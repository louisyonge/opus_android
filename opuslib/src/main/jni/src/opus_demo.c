#include "config.h"


#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "../opustool/opusaudio.h"

void print_use_demo(void)
{
	printf("opus_demo for encode and decoe!\n");
	printf("Usage:\n");
	printf("opus_demo d|e input output option\n");
	printf("d\tdecoding\n");
	printf("e\tencoding\n\n");

}
int main(int argc, char *argv[])
{
	char *input;
	char *output;
	char *option;
	char tmp[1024]={0};
	char op = argv[1][0];


    if (argc < 4 )
    {
       print_use_demo();
       return EXIT_FAILURE;
    }
    else if (argc > 4)
    {
    	option = tmp;
    	int i;
    	char *p = tmp;
    	for (i = 4; i < argc; i++)
    	{
    		strcpy(p, argv[i]);
    		p += strlen(argv[i]) - 1;	//remove '\0'
    	}
    }

    input = argv[2];
    output = argv[3];

    switch (op)
    {
    case 'e':
    	encode(input, output, option);
    	break;
    case 'd':
    	decode(input, output, option);
    	break;
    default:
    	print_use_demo();
    	break;
     }


    return EXIT_SUCCESS;
}
