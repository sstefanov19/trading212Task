import { createFileRoute } from '@tanstack/react-router'
import TradingMode from '../components/TradingMode'

export const Route = createFileRoute('/trade')({
  component: TradingMode,
})
