import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

function ProfitLossChart({ data = [] }) {
  // Default sample data when no data is provided
  const defaultData = [
    { time: '00:00', value: 1000 },
    { time: '04:00', value: 1012 },
    { time: '08:00', value: 998 },
    { time: '12:00', value: 1025 },
    { time: '16:00', value: 1018 },
    { time: '20:00', value: 1035 },
    { time: '24:00', value: 1042 },
  ];

  const rawData = data.length > 0 ? data : defaultData;

  const sampleInterval = rawData.length > 100 ? Math.ceil(rawData.length / 50) : 1;
  const sampledData = rawData.filter((_, i) => i % sampleInterval === 0 || i === rawData.length - 1);

  const isProfit = sampledData[sampledData.length - 1]?.value >= sampledData[0]?.value;

  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart data={sampledData} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#2b3139" />
        <XAxis
          dataKey="time"
          stroke="#848e9c"
          tick={{ fill: '#848e9c', fontSize: 11 }}
          interval={Math.max(0, Math.floor(sampledData.length / 6) - 1)}
        />
        <YAxis
          domain={['dataMin - 50', 'dataMax + 50']}
          stroke="#848e9c"
          tick={{ fill: '#848e9c', fontSize: 12 }}
          tickCount={5}
          tickFormatter={(value) => `$${value.toFixed(0)}`}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: '#1e2329',
            border: '1px solid #2b3139',
            borderRadius: '4px',
          }}
          labelStyle={{ color: '#848e9c' }}
          formatter={(value) => [`$${value.toFixed(2)}`, 'Value']}
        />
        <Line
          type="monotone"
          dataKey="value"
          stroke={isProfit ? '#0ecb81' : '#f6465d'}
          strokeWidth={2}
          dot={false}
          activeDot={{ r: 6, fill: isProfit ? '#0ecb81' : '#f6465d' }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}

export default ProfitLossChart;
