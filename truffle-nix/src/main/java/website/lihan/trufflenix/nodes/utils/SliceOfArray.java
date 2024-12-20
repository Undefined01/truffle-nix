package website.lihan.trufflenix.nodes.utils;

public record SliceOfArray(Object[] array, int start, int count) {
  public SliceOfArray(Object[] array) {
    this(array, 0, array.length);
  }

  public SliceOfArray(Object[] array, int start, int count) {
    this.array = array;
    this.start = start;
    this.count = count;
  }
}
