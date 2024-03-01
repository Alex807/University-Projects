#include <stdio.h> 
#include <stdlib.h> 
#include <math.h> 

void simulation_of_credit(unsigned time, float monthly_rate, float dae, float sold)
{ 
    printf("\nLuna | Achitare dobanda | Achitare sold | Fixed Rate  |  Sold curent\n");
    int i; 
    time=time*12;
    float sum=0, dob=0;
    for(i = 1; i <= time; i++) // integrare time
    { 
        float medium_dae = ((dae / 100) * sold) / 12; 
        float pay_sold = monthly_rate - medium_dae; 
        float remaining_sold = sold - pay_sold;

        sum+=pay_sold;   
        dob+=medium_dae;

        printf("%d \t %10.2f \t %10.2f \t %10.2f \t %10.2f\n", i, medium_dae, pay_sold, medium_dae + pay_sold, remaining_sold);
        sold = remaining_sold; 
    }

    printf("Total principal achitat(RON): %g\nDobanda platita de creditor(RON): %g\n", sum, dob);
}

float calculate_monthly_rate(float dae, unsigned time, double sold_credit) 
{
    dae=dae/100; //for transform from % t  decimal
    // Convert in monthly rate
    double monthly_interests = dae / 12;
    time=time*12; //transform time from years to months

    double value = pow(1 + monthly_interests, time);
    // formula for monthly rate

    return (monthly_interests * value ) / (value - 1) * sold_credit;
}

int main(void)
{ //dae=dobanda anuala efectiva 
  //sold=valoare imprumutata prin credit
    unsigned period_of_time;
    float sold, dae;
    printf("Valuarea creditului(RON): "); 
    scanf("%g", &sold); 

    printf("Perioada de timp(ani): "); 
    scanf("%u",  &period_of_time);

    printf("Dobanda anuala fixa(%%): ");  //use double % for avoiding error at printf
    scanf("%g", &dae);

    float monthly_rate=calculate_monthly_rate(dae, period_of_time, sold); //finding the fix monthly rate for this credit

    simulation_of_credit(period_of_time, monthly_rate, dae, sold);
    return 0;

}