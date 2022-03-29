#!/usr/bin/env python
# coding: utf-8

#imports:
import sys
import copy

import math
from scipy.integrate import solve_ivp
import numpy as np

#overall arguments ODE solver + abstraction :

#for simulating by the scipy ODE solver
maxT = 2.0 #default max time for evolution inside a rectangle
rtol=1e-7

#method in ['RK23', 'RK45', 'Radau', 'BDF', 'LSODA']
Method='LSODA' #method argument is not necessary
t_eval_points_count = 100 #400

#QDA arg
K=3
M=2
#n will be set by argument
n=12 #default value




#python code for get_successors (everything needed)
#approximation by multi-affine using the derivative at vertices
#supposing a rectangle exists this finds it
def find_rectangle( x ):
    r = []
    for i in range(0,n):
        r.append( 0 ) #default values n zeros
    for i in range(0,n):
        for j in range(0, pieces_count):
            if (tresholds[i][j] < x[i]):
                r[i] = j 
    return r

###############################################################

#is the rectangle inside our system of tresholds?
def exists_rectangle( r ):
    exists = True
    for i in range(0,n):
        if r[i] < 0 :
            exists = False
        elif r[i] > (pieces_count-1): #cannot start on the last treshold or higher
            exists = False
    return exists

def is_point_inside_rectangle( x, r ):
    inside = True
    for i in range(0,n):
        minim = (tresholds[i])[ r[i] ]
        maxim = (tresholds[i])[ r[i]+1 ]
        if x[i] < minim: inside = False
        if x[i] > maxim: inside = False
    return inside

#outside rectangle, below lower facet
def is_point_below_facet( x, r, direction ):
    minim_dir = (tresholds[ direction ])[ r[ direction ] ]
    return ( x[ direction ] < minim_dir)

#outside rectangle, above upper facet
def is_point_above_facet( x, r, direction ):
    maxim_dir = (tresholds[ direction ])[ r[ direction ]+1 ]
    return ( x[ direction ] > maxim_dir )

#sharp <0 with outside normal vector to facet
def RA_inside_condition( x, direction, orientation, derivative_func ):
    i_der_at_x = (derivative_func( x ))[ direction ]
    #print("deriv at x="+str(x)+" is="+str(i_der_at_x))
    if orientation == 0 : # |-> |
        #print("RA condition="+str( i_der_at_x > 0 ))
        return ( i_der_at_x > 0 )
    else: # |<-|
        #print("RA condition="+str( i_der_at_x < 0 ))
        return ( i_der_at_x < 0 )
    
#sharp >0 with outside normal vector to facet
def RA_outside_condition( x, direction, orientation, derivative_func ):
    i_der_at_x = (derivative_func( x ))[ direction ]
    if orientation == 0 : # <-| |
        return ( i_der_at_x < 0 )
    else: # | |->
        return ( i_der_at_x > 0 )

#check if 0 vector can be obtained as a convex combination of
#derivative values at corners of rectangle r
def RA_selfloop_condition( r, derivative_func ):
    if not exists_rectangle: 
        return False
    else:
        existsP = [ False ] * n
        existsN = [ False ] * n
        
        rvals = []
        for i in range( 0, n ):
            rvals += [ [ tresholds[ j ][ r[j] ], tresholds[ j ][ r[j] + 1 ]  ] ]
        
        vertices = list_of_points_with_coords( rvals )
       
        for v in vertices:
            fv = derivative_func( v )
            for i in range(0,n):
                if( fv[i] > 0 ): existsP[i] = True
                if( fv[i] < 0 ): existsN[i] = True
        
        result = True
        for ep in existsP:
            result = result and p
        for en in existsN:
            result = result and n
        return result

def which_facets_outside( x, r ): #which is my exit facet of r
    #result = [ [], [] ] #exit facets on dim 0, 1
    result = []
    for i in range(0,n):
        result = result + [ [] ]
    for fdir in range(0,n):
        if is_point_below_facet( x, r, fdir ): #(outside) below lower facet
            result[ fdir ] = [0]
        if is_point_above_facet( x, r, fdir ): #(outside) above upper facet
            result[ fdir ] = [1]
    return result

def exit_point_from_segment( xin, xout, r, outside_facets ):
    new_outside_facets = outside_facets
    xexit_result = xout #default value
    for i in range(0,n):
        if len( outside_facets[i] ) > 0: #i.e. len( outside_facets[i] )==1
            #find exit point on hyperplane
            xexit = [ 0.0 ] * n #default
            xexit[ i ] = (tresholds[i])[ r[i] + outside_facets[i][0]] #constant on facet
            coef = ( xexit[ i ] - xin[ i ] )/( xout[ i ] - xin[ i ] ) 
            for j in range(0,n):
                if j != i:
                    xexit[ j ] = ( xout[ j ] - xin[ j ] ) * coef + xin[ j ]
            #if in the facet (in r), let outside facets be otherwise delete this facet
            if is_point_inside_rectangle( xexit, r ):
                xexit_result = xexit
            else:
                new_outside_facets[i] = []
    return [ xexit_result, new_outside_facets ] #the exit point inside r

def real_vertices( r, fdir, fori ):
    ##real coordinates of (2) vertices of a (1dim) facet
    #real coordinates of (2^{n-1}) vertices of a (n-1)dim facet
    rvals = []
    for i in range(0,n):
        if i == fdir:
            rvals += [ [ tresholds[ i ][ r[i] + fori ] ] ] #a list with 1 el = [ 1tres ]
        else:
            rvals +=  [ [ tresholds[ i ][ r[i] ], tresholds[ i ][ r[i] + 1 ] ] ]
    #real coordinates
    vertices = list_of_points_with_coords( rvals )
    return vertices

###################################################################

def approx_tiles( x, k, r, fdir ): #x was a 0dim real number, now we need ndim vector,
    #supposing n-1 from these n coordinates make sense and that x[fdir] can be whatever
    tilemin = [0] * n
    tilemax = [0] * n
    for i in range(0,n):
        if i != fdir:
            fmin = ( tresholds[ i ] )[ r[ i ] ]
            fmax = ( tresholds[ i ] )[ r[ i ] + 1 ]
            xnorm = ( x[ i ] - fmin ) / ( fmax - fmin )
            tilemin[ i ] = math.floor( k * xnorm )
            tilemax[ i ] = math.ceil( k * xnorm )
            tilemin[ i ] = max( tilemin[ i ], 0 )
            tilemax[ i ] = min( tilemax[ i ], k)
    #return [ tilemin, tilemax ] returned one list, e.g. [0,3], 
    # now [[0,3],[1,2],...] a list for each dim
    tiles = []
    for i in range(0,n):
        if i == fdir:
            tiles = tiles + [[]] #the dir where facet is constant
        else:
            tiles.append( [ tilemin[ i ], tilemax[ i ] ] )
    return tiles

def nonempty_intersection( tiles1, tiles2 ):
    #supposing that tiles have [] in the coord of one dim on the facet
    #checking nonempty intersection on all projections
    intersect = True
    for i in range(0,n):
        if ( len(tiles1[ i ]) > 0 ):
            maxleft = max( tiles1[ i ][ 0 ], tiles2[ i ][ 0 ] )
            minright = min( tiles1[ i ][ 1 ], tiles2[ i ][ 1 ] )
            intersect = intersect and ( maxleft <=  minright )  
    return intersect
    
def get_intersection( tiles1, tiles2 ):
    #tiles are lists of n lists
    result = [ [] ] * n
    if nonempty_intersection( tiles1, tiles2 ):
        for i in range(0,n):
            if ( len( tiles1[i] ) > 0 ):
                result[ i ] = [ max( tiles1[i][0], tiles2[i][0] ), min( tiles1[i][1], tiles2[i][1] ) ]
    return result

def unite_tiles( tiles1, tiles2 ): #overapproximate the union by one tileset
    result = [ [] ] * n
    for i in range(0,n):
        if ( len( tiles1[i] ) > 0 ):
            result[ i ] = [ min( tiles1[i][0], tiles2[i][0] ), max( tiles1[i][1], tiles2[i][1]) ]
    return result

def is_subset_of( subs, supers ):
    #determine per dimension
    #we suppose these are tiles of the same facet with [] at the same dim
    result = True
    for i in range(0,n):
        if ( len(subs[i]) == 0 ):
            result = result and True #no change, but need to list the case, because unindexed subs[i]
                                     #and because the next case can generate False only for nonfacet dirs
        elif ( len(supers[i]) == 0 ): #=[]=emptyset, different from [1,1]=point
            result = False
        else:
            result = result and (subs[i][0] >= supers[i][0]) and (subs[i][1] <= supers[i][1])
    return result

def RA_approved_exit( exit_sets_dict, r, k, ODEfunc_auton ):
    #for exit facets (key) that correspond to a nonempty portion of the entry set (tiles)
    #identify the exit facet from (key)
    #in case key != sefloop, find vertices of exit facet and check RA exit condition
    #    condition not satisfied => exit not possible from this facet, not included in exit sets anymore,
    #    successors will not be calculated from no-exit facets
    #    condition satisfied => exit facet stays in the list
    #in case key == sefloop, we may check RA approved selfloop from the derivatives at vertices of r
    #    condition not satisfied => result of small maxT, issue warning, not include in successors
    #    condition satisfied => selfloop stays
    new_dict = exit_sets_dict
    for key in exit_sets_dict: #a string of two integers "DirOri" or "inside"
        tiles = exit_sets_dict[ key ] #(n-1)dim tiles = [[1,1],[],[0,3]...] or []
        if( len(tiles) > 0 ): #check nonempty entry sets
            #selfloop
            if key == 'inside':
                if not RA_selfloop_condition( r, ODEfunc_auton ):
                    #print( "Rectangle "+str(r)+" is not a RA approved selfloop.")
                    new_dict[ "inside" ] = []
                #else:
                    #print( "Rectangle "+str(r)+" is SELFLOOP." )
                    #print( str(r) )
            #regular exit facet
            else:
                fdir = int( key[0] )
                fori = int( key[1] )
                vertices = real_vertices( r, fdir, fori )
                #print( vertices )
                ra_outside = False
                for v in vertices:
                    if RA_outside_condition( v, fdir, fori, ODEfunc_auton ):
                        ra_outside = True #RA_outside holds at least at one vertex
                if not ra_outside: new_dict[ key ] = []
    return new_dict

#####################################################################
#odesol is a special data type of an ode solution produced by scipy
def get_jth_point( j, odesol ):
    x = []
    for i in range(0,n):
        x.append( odesol.y[i][j] )
    return x

def list_of_points_with_coords( list_of_lists_of_coords ):
    num = len( list_of_lists_of_coords )
    mg = np.meshgrid( *list_of_lists_of_coords )
    
    meshed_lists_per_coord = []
    for i in range( 0, len(list_of_lists_of_coords ) ):
        meshed_lists_per_coord += [ mg[i].flatten('C') ] 
    
    points = []
    for i in range( 0, len( meshed_lists_per_coord[0] ) ):
        point = []
        for j in range( 0, num ):
            point.append( meshed_lists_per_coord[ j ][ i ] )
        points += [ point ]
        
    return points
#get successors routine
#input =  k ... argument for entry set approximation by (a rectangle of) tiles
#         r - rectangle ... list of int coordinates of starting tresholds (<pieces_count), 
#         e - entry set ... [entry var, entry facet], 
#                           dim-1 coordinates of tiles rectangle boundary
#         m - number of simulations per tile
#output = list of successors with entry sets (and labels)

#forward sims only for now

def get_successors( k, r, e_facet, e, ODEfunc, ODEfunc_auton ):
    e_dir,e_or = e_facet
    successors_dict = {}
    for i in range(0,n):
        successors_dict[ str(i)+"0" ] = 0
        successors_dict[ str(i)+"1" ] = 0
    successors_dict[ "inside" ] = 0 #my exit facets
    exit_sets_dict = {}
    for i in range(0,n):
        exit_sets_dict[ str(i)+"0" ] = []
        exit_sets_dict[ str(i)+"1" ] = []
    exit_sets_dict[ "inside" ] = [] #my exit facets : my exit tiles

    points_to_simulate = []
    
    if ( len(e) > 0 ): #the entry set leads through a facet
        #sample the entry set on facet
        #coordinates that are changing on the facet
        #print("n="+str(n))
        rmin_facet = [ 0 ] * n
        rmax_facet = [ 0 ] * n
        Kstep = [ 0 ] * n
        for i in range(0,n):
            if i != e_dir :
                rmin_facet[i] = (tresholds[ i ])[ r[i] ]
                rmax_facet[i] = (tresholds[ i ])[ r[i] + 1 ]
                Kstep[i] = (rmax_facet[i] - rmin_facet[i])/(K*1.0) #float, 
                #Kstep remains 0 for i=e_dir
        #the coordinate that is constant on the facet
        e_const = (tresholds[ e_dir ])[ r[e_dir] + e_or ]
        #the entry set changing coords between
        emin = [ e_const ] * n
        emax = [ e_const ] * n
        #min and max coords from entryset
        for i in range(0,n):
            if i != e_dir:
                emin[i] = rmin_facet[i] + e[i][0]*Kstep[i]
                emax[i] = rmin_facet[i] + e[i][1]*Kstep[i]
        
        rvals = []
        for i in range(0,n):
            rvals_i = []
            #queue = points_to_simulate.copy()
            #points_to_simulate = []
            if i != e_dir:
                if e[i][0] < e[i][1]: #nontrivial 1d entry set
                    Mstep = Kstep[i] / (M*1.0) #float
                    tiles = np.arange( emin[i], emax[i], Kstep[i] )
                    #for all entry tiles simulate and count
                    #ti is a beginning of tile
                    for ti in tiles:
                        for j in range( 0, M ):
                            rvals_i.append( ti + j * Mstep )
                else: #1 point entry set (0dim) e.g. [1,1] projection of entry set
                    rvals_i = [ emin[i] ]
            else:#e_dir
                rvals_i = [ e_const ]
            rvals += [ rvals_i ]
    else: #sample the whole rectangle
        rvals = []
        for i in range(0,n):
            rvals_i = []
            rmin = (tresholds[ i ])[ r[i] ]
            rmax = (tresholds[ i ])[ r[i] + 1 ]
            Kstep = (rmax - rmin)/(K*1.0) #float
            Mstep = Kstep / (M*1.0) #float
            tilestarts = np.arange( rmin, rmax, Kstep )
            #for all entry tiles simulate and count
            #ti is a beginning of tile
            for ti in tilestarts:
                for l in range(0,M):
                    rvals_i.append( ti + l * Mstep )
            rvals += [ rvals_i ]
    points_to_simulate = list_of_points_with_coords( rvals )
    #print( points_to_simulate )
    
    RA_unsat_count = 0
    RA_sat_count = 0
    for x0 in points_to_simulate:
            #for points on entry facet
            #check the RA condition at x00,x01
            #print( "want to simulate from "+str(x0) )
            if (len(e) == 0) or RA_inside_condition( x0, e_dir, e_or, ODEfunc_auton ):
                RA_sat_count += 1
                sol = solve_ivp( ODEfunc, [0.0,maxT], x0, Method, 
                                 t_eval=np.linspace( 0, maxT, t_eval_points_count ),
                                 rtol=rtol )
                #print(sol)
                #where does this trajectory go?
                                                  
                j=1
                x = get_jth_point( j, sol )
                
                while (j < (t_eval_points_count-1)) and is_point_inside_rectangle( x, r ):
                    j += 1
                    x = get_jth_point( j, sol )
                    #print("j="+str(j))
                if is_point_inside_rectangle( x, r ):
                    successors_dict["inside"] = successors_dict["inside"] + 1
                else:
                    #where was the exit/entry point?
                    outside_facets = which_facets_outside( x, r ) #list of my exit facets
                    xin = get_jth_point( j - 1, sol ) #last inside
                    #compute the exit point and real
                    xexit,outside_facets = exit_point_from_segment( xin, x, r, outside_facets )
                    for fdir in [0,1]:
                        if 0 in outside_facets[ fdir ]: #my exit facets
                            successors_dict[ str(fdir)+"0" ] += 1
                            #update exit sets:
                            newtiles = approx_tiles( xexit, k, r, fdir )
                            if exit_sets_dict[ str(fdir)+"0" ] == []:
                                exit_sets_dict[ str(fdir)+"0" ] = newtiles
                            else:
                                exit_sets_dict[ str(fdir)+"0" ] = unite_tiles( newtiles, exit_sets_dict[ str(fdir)+"0" ] )
                        if 1 in outside_facets[ fdir ]:
                            successors_dict[ str(fdir)+"1" ] += 1
                            newtiles = approx_tiles( xexit, k, r, fdir )
                            if exit_sets_dict[ str(fdir)+"1" ] == []:
                                exit_sets_dict[ str(fdir)+"1" ] = newtiles
                            else:
                                exit_sets_dict[ str(fdir)+"1" ] = unite_tiles( newtiles, exit_sets_dict[ str(fdir)+"1" ] )
            else:
                RA_unsat_count += 1

    #print( RA_sat_count )
    #print( RA_unsat_count )
    #print( successors_dict )
    #print( exit_sets_dict )
    exit_sets_dict = RA_approved_exit( exit_sets_dict, r, k, ODEfunc_auton )
    #print( exit_sets_dict )
    #result = [ successors_dict, exit_sets_dict ]
    successors_list = exitsets_dict_into_states_list( r, exit_sets_dict )
    #if successors_dict["inside"]>0: successors_list.append( [r,"inside",[]] )
    result = [ exit_sets_dict, successors_list ]#successors existing rectangles
    return result

#meaningful conversion applicable only to regular facets not selfloops
def key_into_facet( key ):
    if( key == 'inside' ): return 'inside'
    #we suppose n<10...
    return [ int( key[0] ), int( key[1] ) ]

#meaningful conversion only for regular facets
def hash_f( facet ):
    if( facet == 'inside' ): return 'inside'
    return str(facet[0])+str(facet[1])

def hash_r( r ):
    hashr = ""
    for i in range(0,n):                                              
        hashr += str(r[0])
        if i < (n-1):
            hashr += "|"
    return hashr

def r_hash( h ):
    r = [0] * n #default value
    hparts = h.split('|')
    for i in range(0,n):
        r[i] = int( hparts[i] )
    return r

#applicable only to regular facets not selfloops
def key_into_facet_in_successor( key ):
    facet = [0,0]
    facet[0] = int( key[0] ) #this works for n<10
    facet[1] = 1 - int( key[1] ) #opposite facet in successor than that was in the predecessor
    return facet

#applicable only to regular facets not selfloops
def key_into_rectangle( key, r ): #key=facet of r, into successor rectangle of r
    rectangle = r.copy()
    facet = key_into_facet( key ) #dir,ori
    if( facet[1] == '0' ):
        rectangle[ facet[0] ] = rectangle[ facet[0] ] - 1
    else:
        rectangle[ facet[0] ] = rectangle[ facet[0] ] + 1
    return rectangle

#regular states and selfloop
#input = rs exit sets 
#output = list of successors <succ, succs entry set>
def exitsets_dict_into_states_list( r, exit_sets_dict ):
    result = []
    for key in exit_sets_dict:
        if len(exit_sets_dict[ key ]) > 0: #exitsets with nonempty portion of entry set
            if( key == 'inside' ): #set facet as string 'inside'
                result.append( [ r, 'inside', exit_sets_dict[ key ] ] )
            else:
                facet = key_into_facet_in_successor( key )
                rectangle = key_into_rectangle( key, r )#compute the right neighbouring rectangle
                if( exists_rectangle( rectangle )):
                    result.append( [ rectangle, facet, exit_sets_dict[key] ] )
    return result





def set_system( sys_name ):
    if sys_name == "CASE-EXAMPLE-LV-1PAR":
        n = 2
        #ODE for 2 variables and one par
        def ode_func(t, y):
            x0,x1 = y
            return np.array([ 0.1 * x0 - p * x0 * x1, 
                              -0.06 * x1 + 0.2 * x0 * x1 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds
        x0min = 0.1
        x0max = 0.6
        x1min = 0.0
        x1max = 1.0
        xminlist = [x0min,x1min]
        xmaxlist = [x0max,x1max]
        pieces_count = 3
        tresholdsX0 = np.array( [ 0.1, 0.2, 0.4, 0.6 ] )
        tresholdsX1 = np.array( [ 0.0, 0.5, 0.8, 1.0 ] )
        tresholds = [ tresholdsX0, tresholdsX1 ]
        
    elif sys_name == "CASE000aLVPARSQUARED":
        n = 2
        #ODE for 2 variables and one par
        def ode_func(t, y):
            x0,x1 = y
            return np.array([ 0.1 * x0 - p * x0 * x1, 
                              -p * p * x1 + 0.2 * x0 * x1 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds
        x0min = 0.1
        x0max = 0.6
        x1min = 0.0
        x1max = 1.0
        xminlist = [x0min,x1min]
        xmaxlist = [x0max,x1max]
        pieces_count = 3
        tresholdsX0 = np.array( [ 0.1, 0.2, 0.4, 0.6 ] )
        tresholdsX1 = np.array( [ 0.0, 0.5, 0.8, 1.0 ] )
        tresholds = [ tresholdsX0, tresholdsX1 ]
        
    elif sys_name == "CASE000cLV1PARMORETRES":
        n = 2
        #ODE for 2 variables and one par
        def ode_func(t, y):
            x0,x1 = y
            return np.array([ 0.1 * x0 - p * x0 * x1, 
                              -0.06 * x1 + 0.2 * x0 * x1 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds
        x0min = 0.0
        x0max = 0.6
        x1min = 0.0
        x1max = 1.0
        xminlist = [x0min,x1min]
        xmaxlist = [x0max,x1max]
        pieces_count = 50
        stepx0 = ( x0max - x0min ) / pieces_count
        stepx1 = ( x1max - x1min ) / pieces_count
        tresholdsX0 = np.arange ( x0min, x0max+(0.3*stepx0), stepx0 )
        tresholdsX1 = np.arange ( x1min, x1max+(0.3*stepx1), stepx1 )
        tresholds = [ tresholdsX0, tresholdsX1 ]
        
    elif sys_name == "CASE001aSEIR1par":
        n = 4
        #ODE for 4 variables and one par
        def ode_func(t, y):
            x0,x1,x2,x3 = y
            return np.array([ -1.0 * p * x0 * x2 * 0.0000001, 
                              p * x0 * x2 * 0.0000001 - 0.2 * x1,
                              0.2 * x1 - 5.006 * x2,
                              0.2 * x2 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds
        x0min = 0
        x0max = 10000000
        x1min = 0
        x1max = 10000000
        x2min = 0
        x2max = 10000000
        x3min = 0
        x3max = 10000000 
        xminlist = [x0min,x1min,x2min,x3min]
        xmaxlist = [x0max,x1max,x2max,x3max]
        pieces_count = 15
        stepx0 = ( x0max - x0min ) / pieces_count
        stepx1 = ( x1max - x1min ) / pieces_count
        stepx2 = ( x2max - x2min ) / pieces_count
        stepx3 = ( x3max - x3min ) / pieces_count
        tresholdsX0 = np.arange ( x0min, x0max+(0.3*stepx0), stepx0 )
        tresholdsX1 = np.arange ( x1min, x1max+(0.3*stepx1), stepx1 )
        tresholdsX2 = np.arange ( x2min, x2max+(0.3*stepx0), stepx2 )
        tresholdsX3 = np.arange ( x3min, x3max+(0.3*stepx1), stepx3 )
        tresholds = [ tresholdsX0, tresholdsX1, tresholdsX2, tresholdsX3 ]
        
    elif sys_name == "CASE001aSEIRnormSimple1par":
        n = 4
        #ODE for 4 variables and one par
        def ode_func(t, y):
            x0,x1,x2,x3 = y
            return np.array([ -p * x0 * x2, 
                              p * x0 * x2 - 0.2 * x1,
                              0.2 * x1 - x2,
                              x2 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds 10
        x0min = 0
        x0max = 1
        x1min = 0
        x1max = 1
        x2min = 0
        x2max = 1
        x3min = 0
        x3max = 1 
        xminlist = [x0min,x1min,x2min,x3min]
        xmaxlist = [x0max,x1max,x2max,x3max]
        pieces_count = 10
        stepx0 = ( x0max - x0min ) / pieces_count
        stepx1 = ( x1max - x1min ) / pieces_count
        stepx2 = ( x2max - x2min ) / pieces_count
        stepx3 = ( x3max - x3min ) / pieces_count
        tresholdsX0 = np.arange ( x0min, x0max+(0.3*stepx0), stepx0 )
        tresholdsX1 = np.arange ( x1min, x1max+(0.3*stepx1), stepx1 )
        tresholdsX2 = np.arange ( x2min, x2max+(0.3*stepx0), stepx2 )
        tresholdsX3 = np.arange ( x3min, x3max+(0.3*stepx1), stepx3 )
        tresholds = [ tresholdsX0, tresholdsX1, tresholdsX2, tresholdsX3 ]
        

    elif sys_name == "CASE001aSEIRnorm1par":
        n = 4
        #ODE for 4 variables and one par
        def ode_func(t, y):
            x0,x1,x2,x3 = y
            return np.array([ -p * x0 * x2, 
                              p * x0 * x2 - 0.2 * x1,
                              0.2 * x1 - x2,
                              x2 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds 15
        x0min = 0
        x0max = 1
        x1min = 0
        x1max = 1
        x2min = 0
        x2max = 1
        x3min = 0
        x3max = 1 
        xminlist = [x0min,x1min,x2min,x3min]
        xmaxlist = [x0max,x1max,x2max,x3max]
        pieces_count = 15
        stepx0 = ( x0max - x0min ) / pieces_count
        stepx1 = ( x1max - x1min ) / pieces_count
        stepx2 = ( x2max - x2min ) / pieces_count
        stepx3 = ( x3max - x3min ) / pieces_count
        tresholdsX0 = np.arange ( x0min, x0max+(0.3*stepx0), stepx0 )
        tresholdsX1 = np.arange ( x1min, x1max+(0.3*stepx1), stepx1 )
        tresholdsX2 = np.arange ( x2min, x2max+(0.3*stepx0), stepx2 )
        tresholdsX3 = np.arange ( x3min, x3max+(0.3*stepx1), stepx3 )
        tresholds = [ tresholdsX0, tresholdsX1, tresholdsX2, tresholdsX3 ]

    elif sys_name == "CASE003aBRUSSELATOR1par":
        n = 2
        Method = 'RK45'
        #TODO input this case to the Kotlin part 
        #ODE for 2 variables and one par
        def ode_func(t, y):
            x0,x1 = y
            return np.array([ 1.0 + x0 * x0 * x1 - p * x0 - x0, 
                              p * x0 - x0 * x0 * x1 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds
        x0min = 0.0
        x0max = 6.0
        x1min = 0.0
        x1max = 6.0
        xminlist = [x0min,x1min]
        xmaxlist = [x0max,x1max]
        pieces_count = 100
        stepx0 = ( x0max - x0min ) / pieces_count
        stepx1 = ( x1max - x1min ) / pieces_count
        tresholdsX0 = np.arange ( x0min, x0max+(0.3*stepx0), stepx0 )
        tresholdsX1 = np.arange ( x1min, x1max+(0.3*stepx1), stepx1 )
        tresholds = [ tresholdsX0, tresholdsX1 ]

    else: #default system - Van der Pol Oscillator (todo change into
          #parametrized Brusselator...)
        n = 2 #(still unparametrized dummy)
        #ODE for 2 variables and one par
        def ode_func(t, y):
            x0,x1 = y
            return np.array([ x1, 
                              2 * ( 1 - x0 * x0 ) * x1 - x0 ])
        def ode_func_auton(y):
            return ode_func(0,y)
        #tresholds
        x0min = -8
        x0max = 8
        x1min = -8
        x1max = 8
        xminlist = [x0min,x1min]
        xmaxlist = [x0max,x1max]
        pieces_count = 100
        stepx0 = ( x0max - x0min ) / pieces_count
        stepx1 = ( x1max - x1min ) / pieces_count
        tresholdsX0 = np.arange ( x0min, x0max+(0.3*stepx0), stepx0 )
        tresholdsX1 = np.arange ( x1min, x1max+(0.3*stepx1), stepx1 )
        tresholds = [ tresholdsX0, tresholdsX1 ]
    return [ n, ode_func, ode_func_auton, xminlist, xmaxlist, pieces_count, tresholds ]
        



#sysargv=["ntb","CASE003aBRUSSELATOR1par","[10,20]", "1","0","0.0","3.0","0.1","4.0"]
#read arguments and set the input system
biosystem = sys.argv[1]
#[n, ode_func, ode_func_auton, xminlist, xmaxlist, pieces_count, tresholds ]
n,ode_func,ode_func_auton,xminlist,xmaxlist,pieces_count,tresholds = set_system( biosystem )

##rectangle of the state
rstr = sys.argv[2][1:-1] #format "[1,2,...]" -> "1,2,..."
rparts = rstr.split(",")
r=[]
for rp in rparts:
    r.append( int( rp ) )

#entry to rectangle
e_facet = []
e_facet.append( int( sys.argv[3] ) )#dir
e_facet.append( int( sys.argv[4] ) )#ori

#admissible parameter valuations
pinterval = []
pinterval.append( float( sys.argv[5] ) )
pinterval.append( float( sys.argv[6] ) )

#delta = maximal length of divisible interval
delta = float( sys.argv[7] )

#maximal time of simulation
maxT = float( sys.argv[8] )

#print(biosystem)
#print(n)
#print(r)
#print(e_facet)
#print(pinterval)
#print(delta)
#print(maxT)



#compute the successors x parsets

#sample the parameter interval
par_vals = np.arange ( pinterval[0], pinterval[1]+(0.3*delta), delta )

results_per_facet = {}
for i in range(0,n):
    results_per_facet[ str(i)+"0" ] = []
    results_per_facet[ str(i)+"1" ] = []
results_per_facet[ 'inside' ] = []
    
default_entry = [ [0,K] ] * n
default_entry[ e_facet[0] ] = []
    
for pval in par_vals:
    p = pval
    exit_sets,succs = get_successors( K, r, e_facet, default_entry, ode_func, ode_func_auton )
    #print( exit_sets )
    for fkey in results_per_facet:
        results_per_facet[ fkey ].append( ( len(exit_sets[ fkey ]) > 0 ) ) #boolean values

intervals_per_facet = {} #will be lists of intervals of valid p valuations per facet

for fkey in results_per_facet:
    left = -1
    right = -1
    bools = results_per_facet[ fkey ]
    #print(fkey)
    #print(bools)
    val_intervals = []
    i = 0
    while ( i < len( bools ) ):
        foundNext = False
        #find and save next interval of valid valuations
        while ( i < len(bools) ) and ( not bools[i] ):
            i += 1
        if ( i < len( bools ) ): # i.e.  bools[i] is True
            foundNext = True
            left = i
            while ( i < len(bools) ) and bools[i]:
                i += 1
                if( i < len(bools) ):# i.e. bools[i] is False
                    right = i
                else:
                    right = len(bools)-1
        if foundNext:
            left_val = min( par_vals[left], pinterval[1] )
            right_val = min( par_vals[right], pinterval[1] )
            val_intervals = val_intervals + [ [ par_vals[left], right_val ] ]
    if( len(val_intervals) > 0 ): 
        intervals_per_facet[ fkey ] = val_intervals

#interals per facets now contain the lists of intervals of valid valuations of p
#for existence of transitions through the given facets
#print( intervals_per_facet )




#output in the wanted format for kotlin app
#which is: 
#facet or in Kotlin is +-1, in Python 1,0
#print( "1,1 "+"0,-1 "+"0.1,0.5") #successor, parset
#print( "0,0 "+"1,1 "+"0.1,0.5;0.7,1.0") #successor, parset

for fkey in intervals_per_facet:
    if fkey != "inside": #the selfloops are not outputted (not needed now for the reachability purposes)
        #create a successor rectangle from r and fkey
        # a comma-separated list of rectangle^ coord
        r_succ = key_into_rectangle( fkey, r ) #applicable only to regular facets not selfloops
        #key=facet of r, into successor rectangle of r
        r_str = ""
        for i in range(0,n):
            r_str += str( r_succ[i] )
            if i < (n-1): r_str += ","
        
        #create a (dir,ori -1/+1)-style facet _of the successor_ from fkey
        if fkey[1] == '0':
            o_str = "1" #opposite facet in the successor
        else:#'1'
            o_str = "-1"
        f_str = fkey[0] + "," + o_str
        
        #parset like left,right;left,right;...
        p_str = ""
        i = 0
        for i in range( 0, len( intervals_per_facet[ fkey ] ) ):
            inter = intervals_per_facet[ fkey ][ i ]
            p_str += str( inter[0] ) + "," + str( inter[1] )
            if i < ( len( intervals_per_facet[ fkey ] ) - 1 ): p_str += ";"
                
        print( r_str + " " + f_str + " " + p_str )

