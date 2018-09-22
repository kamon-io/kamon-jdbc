package kamon.util;

import java.util.ArrayList;
import java.util.List;

/**
 * String deduplication vs interning test
 */
public class StringDedupTest {
    private static final int MAX_EXPECTED_ITERS = 300;
    private static final int FULL_ITER_SIZE = 100 * 1000;

    //30M entries = 120M RAM (for 300 iters)
    private static List<String> LIST = new ArrayList<>( MAX_EXPECTED_ITERS * FULL_ITER_SIZE );

    public static void main(String[] args) throws InterruptedException {
        //24+24 bytes per String (24 String shallow, 24 char[])
        //136M left for Strings

        //Unique, dedup
        //136M / 2.9M strings = 48 bytes (exactly String size)

        //Non unique, dedup
        //4.9M Strings, 100 char[]
        //136M / 4.9M strings = 27.75 bytes (close to 24 bytes per String + small overhead

        //Non unique, intern
        //We use 120M (+small overhead for 100 strings) until very late, but can't extend ArrayList 3 times - we don't have 360M

        /*
          Run it with: -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+PrintStringDeduplicationStatistics
          Give as much Xmx as you can on your box. This test will show you how long does it take to
          run a single deduplication and if it is run at all.
          To test when deduplication is run, try changing a parameter of Thread.sleep or comment it out.
          You may want to print garbage collection information using -XX:+PrintGCDetails -XX:+PrintGCTimestamps
        */

        //Xmx256M - 29 iterations
        fillUnique();

        /*
         This couple of tests compare string deduplication (first test) with string interning.
         Both tests should be run with the identical Xmx setting. I have tuned the constants in the program
         for Xmx256M, but any higher value is also good enough.
         The point of this tests is to show that string deduplication still leaves you with distinct String
         objects, each of those requiring 24 bytes. Interning, on the other hand, return you existing String
         objects, so the only memory you spend is for the LIST object.
         */

        //Xmx256M - 49 iterations (100 unique strings)
        //fillNonUnique( false );

        //Xmx256M - 299 iterations (100 unique strings)
        //fillNonUnique( true );
    }

    private static void fillUnique() throws InterruptedException {
        int iters = 0;
        final UniqueStringGenerator gen = new UniqueStringGenerator();
        while ( true )
        {
            for ( int i = 0; i < FULL_ITER_SIZE; ++i )
                LIST.add( gen.nextUnique() );
            Thread.sleep( 300 );
            System.out.println( "Iteration " + (iters++) + " finished" );
        }
    }

    private static void fillNonUnique( final boolean intern ) throws InterruptedException {
        int iters = 0;
        final UniqueStringGenerator gen = new UniqueStringGenerator();
        while ( true )
        {
            for ( int i = 0; i < FULL_ITER_SIZE; ++i )
                LIST.add( intern ? gen.nextNonUnique().intern() : gen.nextNonUnique() );
            Thread.sleep( 300 );
            System.out.println( "Iteration " + (iters++) + " finished" );
        }
    }

    private static class UniqueStringGenerator
    {
        private char upper = 0;
        private char lower = 0;

        public String nextUnique()
        {
            final String res = String.valueOf( upper ) + lower;
            if ( lower < Character.MAX_VALUE )
                lower++;
            else
            {
                upper++;
                lower = 0;
            }
            return res;
        }

        public String nextNonUnique()
        {
            final String res = "a" + lower;
            if ( lower < 100 )
                lower++;
            else
                lower = 0;
            return res;
        }
    }
}