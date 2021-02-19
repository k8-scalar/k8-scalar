#include <unistd.h>
#include <stdio.h>
#include <gsl/gsl_math.h>

int main (void) {
    for ( ; ; ) {
    double x = 5.0;
    double y = gsl_expm1(100.0);
    printf ("J0(%g) = %.18e\n", x, y);
    sleep(2);
    }
    return 0;
}