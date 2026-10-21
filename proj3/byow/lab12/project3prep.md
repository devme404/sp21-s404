# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer: My solution adds hexagons row by row, but this method lacks generality and a solid mathematical pattern. The provided implementation cleverly identifies the top-right and bottom-right patterns in tessellating hexagons. I have learned that a better understanding of patterns produces more generalized code.

-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer: Both processes require certain invariants to be maintained. Although arguments such as sizes, offsets, etc. may differ, these invariants must still hold. Therefore, appropriate invariants produce guaranteed output.

-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer: I'd think in terms of unit methods for generating a single room, hallway, and so on. Then the algorithm would orchestrate them to handle random elements.

-----
**What distinguishes a hallway from a room? How are they similar?**

Answer: They are both rectangular, but hallway widths are limited to 1 or 2 tiles.
