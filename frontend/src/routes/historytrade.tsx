import { createFileRoute } from '@tanstack/react-router'
import HistoricalMode from '../components/HistoricalMode'

export const Route = createFileRoute('/historytrade')({
  component: () => <HistoricalMode symbol="ETH" />,
})
