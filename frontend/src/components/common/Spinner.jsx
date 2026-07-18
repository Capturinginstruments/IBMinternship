import React from 'react'
import clsx from 'clsx'
import { Loader2 } from 'lucide-react'

export default function Spinner({ size = 'md', className = '' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12', xl: 'w-16 h-16' }
  return (
    <Loader2 className={clsx('animate-spin text-primary-600 dark:text-primary-400',
      sizes[size], className)} />
  )
}
