      let
        quicksort = list:
          if builtins.length list <= 1
            then list
            else
              let
                pivot = builtins.elemAt list 0;
                rest = builtins.tail list;
                less = builtins.filter (x: x < pivot) rest;
                greater = builtins.filter (x: x >= pivot) rest;
              in
                (quicksort less) ++ [pivot] ++ (quicksort greater);
      in
        quicksort [3 2 1 4 9 5 6 7 8]