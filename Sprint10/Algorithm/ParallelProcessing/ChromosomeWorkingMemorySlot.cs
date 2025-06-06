namespace GeneticAlgorithm.Algorithm.ParallelProcessing;

public class ChromosomeWorkingMemorySlot
{
    private readonly Chromosome[] _memoryBuffer;
    private readonly int _bufferCapacity;
    private int _writeCursor;
    private volatile int _latestVersionIndex;

    public ChromosomeWorkingMemorySlot(int bufferCapacity)
    {
        if (bufferCapacity <= 0)
        {
            throw new ArgumentOutOfRangeException(
                nameof(bufferCapacity),
                "Buffer capacity must be greater than zero."
            );
        }
        _bufferCapacity = bufferCapacity;
        _memoryBuffer = new Chromosome[bufferCapacity];
        _writeCursor = 0;
        _latestVersionIndex = -1;
    }

    public void AddVersion(Chromosome newVersion)
    {
        Chromosome versionToStore = newVersion.Clone();
        int indexToStoreAt = _writeCursor;
        _memoryBuffer[indexToStoreAt] = versionToStore;
        Volatile.Write(ref _latestVersionIndex, indexToStoreAt);
        _writeCursor = (indexToStoreAt + 1) % _bufferCapacity;
    }

    public Chromosome GetLatestVersion()
    {
        int currentIndex = Volatile.Read(ref _latestVersionIndex);

        if (currentIndex == -1)
        {
            return null;
        }

        Chromosome versionInBuff = _memoryBuffer[currentIndex];
        return versionInBuff.Clone();
    }
}
