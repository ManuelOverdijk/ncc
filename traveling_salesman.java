/* To summarize as much as possible, the TSP goes like this: You are a salesman who wants to visit each city in a region (a city is essentially a point on a map). There are 'n' cities in the bounded x and y region, and each city is connected to each city (by assume a straight road). You need to find the shortest possible route among the cities that allows you to visit each city.One of the algorithms I want to use (and I will need to test other algorithms) is Brute Force, which checks every possible route and returns the shortest route. bron: stackoverflow */

import java.util.*;

class SO11703827 {

    private static ArrayList<Integer> bestRoute;

    public static void bruteForceFindBestRoute
        (ArrayList<Integer> r,
         ArrayList<Integer> citiesNotInRoute)
    {
        if(!citiesNotInRoute.isEmpty())
        {
            for(int i = 0; i<citiesNotInRoute.size(); i++)
            {
                Integer justRemoved =
                    (Integer) citiesNotInRoute.remove(0);
                ArrayList<Integer> newRoute =
                    (ArrayList<Integer>) r.clone();
                newRoute.add(justRemoved);

                bruteForceFindBestRoute(newRoute, citiesNotInRoute);
                citiesNotInRoute.add(justRemoved);
            }
        }
        else //if(citiesNotInRoute.isEmpty())
        {
            if(isBestRoute(r))
                bestRoute = r;
        }

    }

    private static boolean isBestRoute(ArrayList<Integer> r) {
        System.out.println(r.toString());
        return false;
    }

    public static void main(String[] args) {
        ArrayList<Integer> lst = new ArrayList<Integer>();
        for (int i = 0; i < 6; ++i)
            lst.add(i);
        ArrayList<Integer> route = new ArrayList<Integer>();
        bruteForceFindBestRoute(route, lst);
    }
}
