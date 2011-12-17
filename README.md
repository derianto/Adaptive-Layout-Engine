Adaptive Layout Engine
----------------------

Demonstrates the ability to layout a set of contents with different sizes in a specified placeholder.

Ordering algorithms:

- Order contents by heights (for row-based config), by widths (for columnar config) -> works very well!

Filtering algorithm:

- In general can filter contents with too small size / too high aspect ratio

Possible algorithms:

- Dumb heuristics (trying different row / columnar configs and allocating spaces corresponding to original size) -> complete, result decent for high number of contents
- Trying different row / columnar configs + gradient descent of penalty of sequences of contents given space allocation to those contents -> doable, probably gets good result
- Dynamic programming -> dimensionality is too high, need agressive optimization.  Mostly concerned with allocating contents to which rows / which columns
- A* Search
- Genetic algorithm

Possible layouting variations:

- How if we want to display a container per content, containing image, text, etc.?
- How if we want to add and delete contents dynamically?
- How if we care about relevance of the contents, e.g. the most important content has to appear on the top and appear bigger than other less important contents?

